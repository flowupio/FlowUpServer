package datasources.database;

import com.avaje.ebean.ExpressionList;
import models.ApiKey;
import models.Organization;
import usecases.repositories.ApiKeyRepository;

import javax.inject.Inject;
import java.util.UUID;

public class OrganizationDatasource {

    private final ApiKeyRepository apiKeyRepository;

    @Inject
    public OrganizationDatasource(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
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

    public Organization create(String name, String googleAccount) {
        String billingId = UUID.randomUUID().toString().replaceAll("-", "");
        return create(name, googleAccount, apiKeyRepository.create(), billingId);
    }

    public Organization create(String name, String googleAccount, ApiKey apiKey, String billingId) {
        final Organization organization = new Organization();
        organization.setName(name);
        organization.setApiKey(apiKey);
        if (googleAccount != null) {
            organization.setGoogleAccount(googleAccount);
        }
        organization.setBillingId(billingId);

        organization.save();
        return organization;
    }
}
