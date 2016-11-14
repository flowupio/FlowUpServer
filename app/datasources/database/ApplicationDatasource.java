package datasources.database;

import models.ApiKey;
import models.Application;

public class ApplicationDatasource {
    public boolean existByApiKeyAndAppPackage(String apiKey, String appPackage, String organizationId) {
        Application application = findApplicationByPackageAndOrgId(appPackage, organizationId);
        return application != null && apiKey.equals(application.getOrganization().getApiKey().getValue());
    }

    private Application findApplicationByPackageAndOrgId(String appPackage, String organizationId) {
        return Application.find
                .fetch("organization")
                .fetch("organization.apiKey")
                .where()
                .eq("appPackage", appPackage)
                .and()
                .eq("organization_id", organizationId)
                .findUnique();
    }

    public Application create(String appPackage, ApiKey apiKey) {
        Application application = new Application();
        application.setAppPackage(appPackage);
        application.setOrganization(apiKey.getOrganization());
        application.save();
        return application;
    }
}
