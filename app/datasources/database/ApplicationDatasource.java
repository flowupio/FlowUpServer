package datasources.database;

import models.ApiKey;
import models.Application;

import java.util.UUID;

public class ApplicationDatasource {
    public boolean existByApiKeyValue(String appPackage) {
        return Application.find.where().eq("app_package", appPackage).findRowCount() > 0;
    }

    public Application findByApiKeyValue(String appPackage) {
        return Application.find.where().eq("app_package", appPackage).findUnique();
    }

    public Application create(String appPackage, ApiKey apiKey) {
        Application application = new Application();
        application.setAppPackage(appPackage);
        application.setOrganization(apiKey.getOrganization());
        application.save();
        return application;
    }
}
