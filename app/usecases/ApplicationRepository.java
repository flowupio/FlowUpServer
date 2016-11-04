package usecases;

import datasources.database.ApiKeyDatasource;
import datasources.database.ApplicationDatasource;
import datasources.grafana.GrafanaClient;
import models.ApiKey;
import models.Application;
import play.cache.CacheApi;

import javax.inject.Inject;

import static java.util.stream.Collectors.toList;

class ApplicationRepository {
    private final ApiKeyDatasource apiKeyDatasource;
    private final ApplicationDatasource applicationDatasource;
    private final GrafanaClient grafanaClient;
    private final CacheApi cacheApi;

    @Inject
    ApplicationRepository(ApiKeyDatasource apiKeyDatasource, ApplicationDatasource applicationDatasource, GrafanaClient grafanaClient, CacheApi cacheApi) {
        this.apiKeyDatasource = apiKeyDatasource;
        this.applicationDatasource = applicationDatasource;
        this.grafanaClient = grafanaClient;
        this.cacheApi = cacheApi;
    }

    boolean exist(String apiKey, String appPackage) {
        return cacheApi.getOrElse("exist-" + apiKey + "-" + appPackage, () -> applicationDatasource.existByApiKeyAndAppPackage(apiKey, appPackage));
    }

    Application create(String apiKeyValue, String appPackage) {
        ApiKey apiKey = apiKeyDatasource.findByApiKeyValue(apiKeyValue);

        Application application = applicationDatasource.create(appPackage, apiKey);

        grafanaClient.createOrg(application).thenApply(grafanaResponse -> {

            grafanaClient.createDatasource(application);

            return application.getOrganization().getMembers().stream().map(user -> {
                return grafanaClient.addUserToOrganisation(user, application).thenApply(grafanaResponse1 -> {
                    return grafanaClient.deleteUserInDefaultOrganisation(user);
                });
            }).collect(toList());
        });

        return application;
    }
}
