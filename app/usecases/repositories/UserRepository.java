package usecases.repositories;

import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.EmailIdentity;
import datasources.database.OrganizationDatasource;
import datasources.database.UserDatasource;
import usecases.DashboardsClient;
import models.Organization;
import models.User;
import usecases.EmailDatasource;

import javax.inject.Inject;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class UserRepository {
    private final UserDatasource userDatasource;
    private final OrganizationDatasource organizationDatasource;
    private final DashboardsClient dashboardsClient;
    private final EmailDatasource emailDatasource;

    @Inject
    public UserRepository(UserDatasource userDatasource, OrganizationDatasource organizationDatasource, DashboardsClient dashboardsClient, EmailDatasource emailDatasource) {
        this.userDatasource = userDatasource;
        this.organizationDatasource = organizationDatasource;
        this.dashboardsClient = dashboardsClient;
        this.emailDatasource = emailDatasource;
    }

    public CompletionStage<User> create(AuthUser authUser) {
        boolean isActive = this.existsOrganizationByEmail(authUser);
        User user = userDatasource.create(authUser, isActive);
        CompletionStage<Boolean> completionStage = CompletableFuture.completedFuture(true);
        if (!isActive) {
            completionStage = emailDatasource.sendSigningUpDisabledMessage(user);
        }
        Organization organization = findOrCreateOrganization(user);
        if (organization != null) {
            user = joinOrganization(user, organization);
        }

        final User finalUser = user;
        return completionStage.thenCompose(emailSuccess -> {
            return dashboardsClient.createUser(finalUser).thenCompose(userWithGrafana -> {
                return joinApplicationDashboards(userWithGrafana, organization);
            });
        });
    }

    private CompletionStage<User> joinApplicationDashboards(User user, Organization organization) {
        if (organization.getApplications().isEmpty()) {
            return CompletableFuture.completedFuture(user);
        }
        return addUserToApplications(user, organization).thenCompose(aVoid -> {
            return dashboardsClient.switchUserContext(user, organization.getApplications().get(0)).thenCompose(application -> {
                return dashboardsClient.deleteUserInDefaultOrganisation(user);
            });
        });
    }

    private CompletableFuture<Void> addUserToApplications(User user, Organization organization) {
        CompletableFuture[] completionStages = organization.getApplications().stream().map(application -> {
            return dashboardsClient.addUserToOrganisation(user, application);
        }).toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(completionStages);
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
}
