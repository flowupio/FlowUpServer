package controllers;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.service.UserService;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

import javax.inject.Inject;

@Security.Authenticated(Secured.class)
public class CommandCenterController extends Controller {

    @Inject
    private final PlayAuthenticate auth;

    @Inject
    private final UserService userService;

    public Result index() {
        final User localUser = this.userService.getLocalUser(this.auth.getUser(session()));
        return ok(restricted.render(this.auth, localUser));
    }
}
