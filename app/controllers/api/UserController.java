package controllers.api;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;
import controllers.Secured;
import models.Organization;
import models.PublicUser;
import models.User;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import usecases.GetPrimaryOrganization;
import usecases.GetUserByAuthUserIdentity;

import java.util.concurrent.CompletionStage;

@Security.Authenticated(Secured.class)
public class UserController extends Controller {

    private final PlayAuthenticate auth;
    private final GetUserByAuthUserIdentity getUserByAuthUserIdentity;
    private final GetPrimaryOrganization getPrimaryOrganization;

    public UserController(PlayAuthenticate auth, GetUserByAuthUserIdentity getUserByAuthUserIdentity, GetPrimaryOrganization getPrimaryOrganization) {
        this.auth = auth;
        this.getUserByAuthUserIdentity = getUserByAuthUserIdentity;
        this.getPrimaryOrganization = getPrimaryOrganization;
    }

    public CompletionStage<Result> get() {
        AuthUser authUser = auth.getUser(session());
        User user = getUserByAuthUserIdentity.execute(authUser);
        return getPrimaryOrganization.execute(user).thenApply(organization -> {
                    PublicUser publicUser = mapPublicUser(user, organization);
                    return ok(Json.toJson(publicUser));
                }
        );
    }

    private PublicUser mapPublicUser(User user, Organization organization) {
        return new PublicUser(user.getId(), user.getName(), user.getEmail(), organization.hasApplications());
    }

}
