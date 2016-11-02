package datasources.database;

import com.avaje.ebean.ExpressionList;
import models.Organization;

import javax.inject.Inject;

public class OrganizationDatasource {
    private final ApiKeyDatasource apiKeyDatasource;

    @Inject
    public OrganizationDatasource(ApiKeyDatasource apiKeyDatasource) {
        this.apiKeyDatasource = apiKeyDatasource;
    }


    public Organization findByGoogleAccount(String googleAccount) {
        return getGoogleAccountUserFind(googleAccount).findUnique();
    }

    private ExpressionList<Organization> getGoogleAccountUserFind(String googleAccount) {
        return Organization.find.where().eq("google_account", googleAccount);
    }

    public Organization create(String name) {
        return create(name, null);
    }

    public Organization create(String name, String gooogleAccount) {
        final Organization organization = new Organization();
        organization.setName(name);
        organization.setApiKey(apiKeyDatasource.create());
        if (gooogleAccount != null) {
            organization.setGoogleAccount(gooogleAccount);
        }

        organization.save();
        return organization;
    }
}
