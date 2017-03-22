package usecases.repositories;

import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.feth.play.module.pa.user.EmailIdentity;
import datasources.database.OrganizationDatasource;
import datasources.database.UserDatasource;
import usecases.DashboardsClient;
import models.Organization;
import models.User;
import usecases.EmailSender;

import javax.inject.Inject;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class UserRepository {
    private final UserDatasource userDatasource;
    private final OrganizationDatasource organizationDatasource;

    @Inject
    public UserRepository(UserDatasource userDatasource, OrganizationDatasource organizationDatasource, DashboardsClient dashboardsClient, EmailSender emailSender) {
        this.userDatasource = userDatasource;
        this.organizationDatasource = organizationDatasource;
    }

    public CompletionStage<User> joinOrganization(final User user) {
        return CompletableFuture.supplyAsync(() -> {
            Organization organization = findOrCreateOrganization(user);
            if (organization != null) {
                return joinOrganization(user, organization);
            }
            return user;
        });
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

    public boolean existsOrganizationByEmail(AuthUser authUser) {
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

    public User create(AuthUser authUser, boolean isActive) {
        return userDatasource.create(authUser, isActive);
    }

    public User getById(UUID userId) {
        return userDatasource.findById(userId);
    }

    public void setActive(User user) {
        userDatasource.setActive(user);
    }

    public User getByAuthUserIdentity(AuthUserIdentity identity) {
        return User.findByAuthUserIdentity(identity);
    }
}
