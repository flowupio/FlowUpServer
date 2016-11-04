package datasources.database;

import models.ApiKey;
import models.Application;

public class ApplicationDatasource {
    public boolean existByApiKeyAndAppPackage(String apiKey, String appPackage) {
        Application application = findByAppPackage(appPackage);
        return application != null && apiKey.equals(application.getOrganization().getApiKey().getValue());
    }

    public Application findByAppPackage(String appPackage) {
        return Application.find
                .fetch("organization")
                .fetch("organization.apiKey")
                .where().eq("appPackage", appPackage).findUnique();
    }

    public Application create(String appPackage, ApiKey apiKey) {
        Application application = new Application();
        application.setAppPackage(appPackage);
        application.setOrganization(apiKey.getOrganization());
        application.save();
        return application;
    }
}
