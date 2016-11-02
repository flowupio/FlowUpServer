package repositories;

import com.feth.play.module.pa.user.AuthUser;
import datasources.database.OrganizationDatasource;
import datasources.database.UserDatasource;
import datasources.grafana.GrafanaClient;
import models.Organization;
import models.User;
import play.Logger;
import play.cache.CacheApi;

import javax.inject.Inject;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

public class UserRepository {
    private final UserDatasource userDatasource;
    private final OrganizationDatasource organizationDatasource;
    private final CacheApi cacheApi;
    private final GrafanaClient grafanaClient;

    @Inject
    public UserRepository(UserDatasource userDatasource, OrganizationDatasource organizationDatasource, CacheApi cacheApi, GrafanaClient grafanaClient) {
        this.userDatasource = userDatasource;
        this.organizationDatasource = organizationDatasource;
        this.cacheApi = cacheApi;
        this.grafanaClient = grafanaClient;
    }

    public User create(AuthUser authUser) {
        User user = userDatasource.create(authUser);
        createOrAddToOrg(user);
        createGrafanaUser(user);
        return user;
    }


    private void createOrAddToOrg(User user) {
        String[] split = user.getEmail().split("@");
        if (split.length == 2) {
            String domain = split[1];
            if ("gmail.com".equals(domain) || "googlemail.com".equals(domain)) {
                organizationDatasource.create(split[0]);
            } else {
                String googleAccount = "@" + domain;
                Organization organization = organizationDatasource.findByGoogleAccount(googleAccount);
                if (organization == null) {
                    organization = organizationDatasource.create(domain, googleAccount);
                }
                user.setOrganizations(Collections.singletonList(organization));
                user.update();
            }
        }
    }

    private void createGrafanaUser(User user) {
        try {
            grafanaClient.createUser(user).toCompletableFuture().get();
        } catch (InterruptedException e) {
            Logger.debug(e.getMessage());
        } catch (ExecutionException e) {
            Logger.debug(e.getMessage());
        }
    }

//    private void addUserToGrafanaOrg(User user) {
//        String[] split = user.getEmail().split("@");
//        if (split.length == 2) {
//            Organization organization = OrganizationDatasource.findByGoogleAccount("@" + split[1]);
//            try {
//                grafanaClient.addUserToOrganisation(user, organization).toCompletableFuture().get();
//                user.refresh();
//                grafanaClient.deleteUserInDefaultOrganisation(user).toCompletableFuture().get();
//            } catch (InterruptedException e) {
//                Logger.debug(e.getMessage());
//            } catch (ExecutionException e) {
//                Logger.debug(e.getMessage());
//            }
//        }
//    }
}
