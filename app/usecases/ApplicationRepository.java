package usecases;

import datasources.database.ApiKeyDatasource;
import datasources.database.ApplicationDatasource;
import datasources.grafana.DashboardsClient;
import models.ApiKey;
import models.Application;
import org.jetbrains.annotations.NotNull;
import play.cache.CacheApi;

import javax.inject.Inject;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ApplicationRepository {
    private final ApiKeyDatasource apiKeyDatasource;
    private final ApplicationDatasource applicationDatasource;
    private final DashboardsClient dashboardsClient;
    private final CacheApi cacheApi;

    @Inject
    ApplicationRepository(ApiKeyDatasource apiKeyDatasource, ApplicationDatasource applicationDatasource, DashboardsClient dashboardsClient, CacheApi cacheApi) {
        this.apiKeyDatasource = apiKeyDatasource;
        this.applicationDatasource = applicationDatasource;
        this.dashboardsClient = dashboardsClient;
        this.cacheApi = cacheApi;
    }

    boolean exist(String apiKey, String appPackage) {
        String cacheKey = getCacheKey(apiKey, appPackage);
        return cacheApi.getOrElse(cacheKey, () -> applicationDatasource.existByApiKeyAndAppPackage(apiKey, appPackage));
    }

    private String getCacheKey(String apiKey, String appPackage) {
        return "exist-" + apiKey + "-" + appPackage;
    }

    public CompletionStage<Application> create(String apiKeyValue, String appPackage) {
        ApiKey apiKey = apiKeyDatasource.findByApiKeyValue(apiKeyValue);
        if (apiKey == null) {
            return null;
        }

        Application application = applicationDatasource.create(appPackage, apiKey);

        String cacheKey = getCacheKey(apiKeyValue, appPackage);
        cacheApi.set(cacheKey, true);

        return dashboardsClient.createOrg(application).thenCompose(applicationWithOrg -> {

            CompletionStage<Void> datasourceCompletionStage = dashboardsClient.createDatasource(applicationWithOrg)
                    .thenCompose(this::addUsersToApplicationDashboards);

            return datasourceCompletionStage.thenApply(result -> applicationWithOrg);
        });
    }

    @NotNull
    private CompletionStage<Void> addUsersToApplicationDashboards(Application application) {
        CompletableFuture[] completionStages = application.getOrganization().getMembers().stream().map(user -> {
            return dashboardsClient.addUserToOrganisation(user, application).thenCompose(grafanaResponse1 -> {
                return dashboardsClient.deleteUserInDefaultOrganisation(user);
            }).toCompletableFuture();
        }).toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(completionStages);
    }
}
