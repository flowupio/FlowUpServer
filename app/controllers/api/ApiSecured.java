package controllers.api;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;
import play.mvc.Http.Context;
import play.mvc.Result;
import play.mvc.Security;

import javax.inject.Inject;

public class ApiSecured extends Security.Authenticator {

    private final PlayAuthenticate auth;

    @Inject
    public ApiSecured(final PlayAuthenticate auth) {
        this.auth = auth;
    }

    @Override
    public String getUsername(final Context ctx) {
        final AuthUser u = this.auth.getUser(ctx.session());

        if (u != null) {
            return u.getId();
        } else {
            return null;
        }
    }

    @Override
    public Result onUnauthorized(final Context ctx) {
        return unauthorized();
    }
}
