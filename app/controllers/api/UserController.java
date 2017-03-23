package controllers.api;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;
import models.Organization;
import models.PublicUser;
import models.User;
import models.UserToPublicUserMapper;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import usecases.GetPrimaryOrganization;
import usecases.GetUserByAuthUserIdentity;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

@Security.Authenticated(ApiCookieSecured.class)
public class UserController extends Controller {

    private final PlayAuthenticate auth;
    private final GetUserByAuthUserIdentity getUserByAuthUserIdentity;
    private final GetPrimaryOrganization getPrimaryOrganization;
    private final UserToPublicUserMapper mapper;

    @Inject
    public UserController(PlayAuthenticate auth, GetUserByAuthUserIdentity getUserByAuthUserIdentity, GetPrimaryOrganization getPrimaryOrganization) {
        this.auth = auth;
        this.getUserByAuthUserIdentity = getUserByAuthUserIdentity;
        this.getPrimaryOrganization = getPrimaryOrganization;
        this.mapper = new UserToPublicUserMapper();
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
        return mapper.map(user, organization);
    }

}
