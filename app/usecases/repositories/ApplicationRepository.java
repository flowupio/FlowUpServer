package usecases.repositories;

import datasources.database.ApplicationDatasource;
import models.ApiKey;
import models.Application;
import models.Organization;
import models.Platform;
import org.jetbrains.annotations.NotNull;
import play.cache.CacheApi;
import usecases.DashboardsClient;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ApplicationRepository {
    private static final String APPLICATION_APP_PACKAGE_ORGANIZATION_ID_CACHE_KEY = "application.appPackage:%s.organizationId:%s";
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

    public Application getApplicationByApiKeyValueAndAppPackage(String apiKeyValue, String appPackage, Platform platform) {
        ApiKey apiKey = apiKeyRepository.getApiKey(apiKeyValue);
        if (apiKey == null) {
            return null;
        }
        Organization organization = apiKey.getOrganization();
        if (organization == null) {
            return null;
        }
        String organizationId = organization.getId().toString();

        return getApplicationByPackageAndOrgId(appPackage, organizationId, platform);
    }

    private Application getApplicationByPackageAndOrgId(String appPackage, String organizationId, Platform platform) {
        String normalizedAppPackage = normalizeAppPackage(appPackage, platform);
        return cacheApi.getOrElse(getCacheKey(appPackage, organizationId), () ->
                applicationDatasource.findApplicationByPackageAndOrgId(normalizedAppPackage, organizationId));
    }

    private String getCacheKey(String appPackage, String organizationId) {
        return String.format(APPLICATION_APP_PACKAGE_ORGANIZATION_ID_CACHE_KEY, appPackage, organizationId);
    }

    public CompletionStage<Application> create(String apiKeyValue, String appPackage, Platform platform) {
        ApiKey apiKey = apiKeyRepository.getApiKey(apiKeyValue);
        if (apiKey == null) {
            return null;
        }

        String normalizedAppPackage = normalizeAppPackage(appPackage, platform);

        Application application = applicationDatasource.create(normalizedAppPackage, apiKey);

        String cacheKey = getCacheKey(apiKeyValue, normalizedAppPackage);
        cacheApi.set(cacheKey, true);

        return dashboardsClient.createOrg(application)
                .thenCompose(applicationWithOrg -> {

                    CompletionStage<Void> datasourceCompletionStage = dashboardsClient.createDatasource(applicationWithOrg)
                            .thenCompose(application1 ->
                                    dashboardsClient.createDashboards(application1, platform)
                                            .thenCompose(v -> addUsersToApplicationDashboards(application1)));

                    return datasourceCompletionStage.thenApply(result -> applicationWithOrg);
                });
    }

    @NotNull
    private CompletionStage<Void> addUsersToApplicationDashboards(Application application) {
        CompletableFuture[] completionStages = application.getOrganization().getMembers().stream().map(user -> {
            return dashboardsClient.addUserToOrganisation(user, application).thenCompose(application1 -> {
                return dashboardsClient.switchUserContext(user, application1).thenCompose(application2 -> {
                    return dashboardsClient.deleteUserInDefaultOrganisation(user).thenCompose(user1 -> {
                        return dashboardsClient.updateHomeDashboard(user1, application2);
                    });
                });
            }).toCompletableFuture();
        }).toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(completionStages);
    }

    public Application findById(UUID id) {
        return Application.find.byId(id);
    }

    public CompletableFuture<List<Application>> findAll() {
        return applicationDatasource.findAll();
    }

    private String normalizeAppPackage(String appPackage, Platform platform) {
        switch (platform) {
            case IOS:
                return appPackage + Application.IOS_APPLICATION_SUFFIX;
            case ANDROID:
            default:
                return appPackage;
        }
    }
}
