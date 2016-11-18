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
    public static final String APPLICATION_APP_PACKAGE_ORGANIZATION_ID_CACHE_KEY = "application.appPackage:%s.organizationId:%s";
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

    public Application getApplicationByApiKeyValueAndAppPackage(String apiKeyValue, String appPackage) {
        ApiKey apiKey = apiKeyRepository.getApiKey(apiKeyValue);
        if (apiKey == null) {
            return null;
        }
        Organization organization = apiKey.getOrganization();
        if (organization == null) {
            return null;
        }
        String organizationId = organization.getId().toString();

        return getApplicationByPackageAndOrgId(appPackage, organizationId);
    }

    private Application getApplicationByPackageAndOrgId(String appPackage, String organizationId) {
        return cacheApi.getOrElse(getCacheKey(appPackage, organizationId), () ->
                applicationDatasource.findApplicationByPackageAndOrgId(appPackage, organizationId));
    }

    private String getCacheKey(String appPackage, String organizationId) {
        return String.format(APPLICATION_APP_PACKAGE_ORGANIZATION_ID_CACHE_KEY, appPackage, organizationId);
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
            return dashboardsClient.addUserToOrganisation(user, application).thenCompose(grafanaResponse1 -> {
                return dashboardsClient.deleteUserInDefaultOrganisation(user);
            }).toCompletableFuture();
        }).toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(completionStages);
    }

    public Application findById(UUID id) {
        return Application.find.byId(id);
    }
}
