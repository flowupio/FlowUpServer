package repositories;

import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.EmailIdentity;
import datasources.database.OrganizationDatasource;
import datasources.database.UserDatasource;
import datasources.grafana.DashboardsClient;
import models.Application;
import models.LinkedAccount;
import models.Organization;
import models.User;
import play.Logger;

import javax.inject.Inject;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

public class UserRepository {
    private final UserDatasource userDatasource;
    private final OrganizationDatasource organizationDatasource;
    private final DashboardsClient dashboardsClient;

    @Inject
    public UserRepository(UserDatasource userDatasource, OrganizationDatasource organizationDatasource, DashboardsClient dashboardsClient) {
        this.userDatasource = userDatasource;
        this.organizationDatasource = organizationDatasource;
        this.dashboardsClient = dashboardsClient;
    }

    public User create(AuthUser authUser) {
        boolean isActive = this.existsOrganizationByEmail(authUser);
        User user = userDatasource.create(authUser, isActive);
        Organization organization = findOrCreateOrganization(user);
        if (organization != null) {
            user = joinOrganization(user, organization);
        }
        createGrafanaUser(user);
        joinApplicationDashboards(user, organization);
        return user;
    }

    private void joinApplicationDashboards(User user, Organization organization) {
        organization.getApplications().forEach(application -> {
            dashboardsClient.addUserToOrganisation(user, application);
        });
        dashboardsClient.deleteUserInDefaultOrganisation(user);
    }


    private Organization findOrCreateOrganization(User user) {
        String email = user.getEmail();
        if (email != null) {
            String[] split = email.split("@");
            if (split.length == 2) {
                String domain = split[1];
                Organization organization;
                if ("gmail.com".equals(domain) || "googlemail.com".equals(domain)) {
                    organization = organizationDatasource.create(split[0]);
                } else {
                    String googleAccount = "@" + domain;
                    organization = organizationDatasource.findByGoogleAccount(googleAccount);
                    if (organization == null) {
                        organization = organizationDatasource.create(domain, googleAccount);
                    }
                }
                return organization;
            }
        }
        return null;
    }

    private boolean existsOrganizationByEmail(AuthUser authUser) {
        if (authUser instanceof EmailIdentity) {
            EmailIdentity identity = (EmailIdentity) authUser;

            String[] split = identity.getEmail().split("@");
            if (split.length == 2) {
                String domain = split[1];
                String googleAccount = "@" + domain;
                Organization organization = organizationDatasource.findByGoogleAccount(googleAccount);
                return organization != null;
            }
        }
        return false;
    }

    private User joinOrganization(User user, Organization organization) {
        user.setOrganizations(Collections.singletonList(organization));
        user.update();
        return user;
    }

    private void createGrafanaUser(User user) {
        try {
            dashboardsClient.createUser(user).toCompletableFuture().get();
        } catch (InterruptedException e) {
            Logger.debug(e.getMessage());
        } catch (ExecutionException e) {
            Logger.debug(e.getMessage());
        }
    }
}
