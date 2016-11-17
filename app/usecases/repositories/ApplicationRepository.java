package usecases.repositories;

import datasources.database.ApplicationDatasource;
import datasources.grafana.DashboardsClient;
import models.ApiKey;
import models.Application;
import models.Organization;
import org.jetbrains.annotations.NotNull;
import play.cache.CacheApi;

import javax.inject.Inject;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ApplicationRepository {
    private final ApiKeyRepository apiKeyRepository;
    private final ApplicationDatasource applicationDatasource;
    private final DashboardsClient dashboardsClient;
    private final CacheApi cacheApi;

    @Inject
    ApplicationRepository(ApiKeyRepository apiKeyRepository, ApplicationDatasource applicationDatasource, DashboardsClient dashboardsClient, CacheApi cacheApi) {
        this.apiKeyRepository = apiKeyRepository;
        this.applicationDatasource = applicationDatasource;
        this.dashboardsClient = dashboardsClient;
        this.cacheApi = cacheApi;
    }

    public boolean exist(String apiKeyValue, String appPackage) {
        ApiKey apiKey = apiKeyRepository.getApiKey(apiKeyValue);
        if (apiKey == null) {
            return false;
        }
        Organization organization = apiKey.getOrganization();
        if (organization == null) {
            return false;
        }
        String organizationId = organization.getId().toString();
        String cacheKey = getCacheKey(apiKeyValue, appPackage);
        return cacheApi.getOrElse(cacheKey, () -> applicationDatasource.existByApiKeyAndAppPackage(apiKeyValue, appPackage, organizationId));
    }

    private String getCacheKey(String apiKey, String appPackage) {
        return "exist-" + apiKey + "-" + appPackage;
    }

    public CompletionStage<Application> create(String apiKeyValue, String appPackage) {
        ApiKey apiKey = apiKeyRepository.getApiKey(apiKeyValue);
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
            return dashboardsClient.addUserToOrganisation(user, application).thenCompose(application1 -> {
                return dashboardsClient.switchUserContext(user, application).thenCompose(application2 -> {
                    return dashboardsClient.deleteUserInDefaultOrganisation(user);
                });
            }).toCompletableFuture();
        }).toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(completionStages);
    }

    public Application findById(UUID id) {
        return Application.find.byId(id);
    }
}
