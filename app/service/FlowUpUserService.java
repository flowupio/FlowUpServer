package service;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.service.AbstractUserService;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import datasources.grafana.GrafanaClient;
import models.Organization;
import models.User;
import play.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.ExecutionException;

@Singleton
public class FlowUpUserService extends AbstractUserService {

    private final GrafanaClient grafanaClient;

    @Inject
    public FlowUpUserService(PlayAuthenticate auth, GrafanaClient grafanaClient) {
        super(auth);
        this.grafanaClient = grafanaClient;
    }

    @Override
    public Object save(AuthUser authUser) {
        final boolean isLinked = User.existsByAuthUserIdentity(authUser);
        if (!isLinked) {
            User user = User.create(authUser);
            createGrafanaUser(user);
            return user.getId();
        } else {
            User user = User.findByAuthUserIdentity(authUser);
            createGrafanaUser(user);
            addUserToGrafanaOrg(user);
            // we have this user already, so return null
            return null;
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

    private void addUserToGrafanaOrg(User user) {
        String[] split = user.getEmail().split("@");
        if (split.length == 2) {
            Organization organization = Organization.findByGoogleAccount("@" + split[1]);
            try {
                grafanaClient.addUserToOrganisation(user, organization).toCompletableFuture().get();
                user.refresh();
                grafanaClient.deleteUserInDefaultOrganisation(user).toCompletableFuture().get();
            } catch (InterruptedException e) {
                Logger.debug(e.getMessage());
            } catch (ExecutionException e) {
                Logger.debug(e.getMessage());
            }
        }
    }

    @Override
    public Object getLocalIdentity(final AuthUserIdentity identity) {
        // For production: Caching might be a good idea here...
        // ...and dont forget to sync the cache when users get deactivated/deleted
        final User u = User.findByAuthUserIdentity(identity);
        if(u != null) {
            return u.getId();
        } else {
            return null;
        }
    }

    @Override
    public AuthUser merge(final AuthUser newUser, final AuthUser oldUser) {
        if (!oldUser.equals(newUser)) {
            User.merge(oldUser, newUser);
        }
        return oldUser;
    }

    @Override
    public AuthUser link(final AuthUser oldUser, final AuthUser newUser) {
        User.addLinkedAccount(oldUser, newUser);
        return null;
    }

}