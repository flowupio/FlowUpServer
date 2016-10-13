package controllers;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.service.UserService;
import models.User;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

import javax.inject.Inject;

@Security.Authenticated(Secured.class)
public class CommandCenterController extends Controller {

    private final PlayAuthenticate auth;

    private final UserService userService;

    @Inject
    public CommandCenterController(final PlayAuthenticate auth, final UserService userService) {
        this.auth = auth;
        this.userService = userService;
    }

    public Result index() {
        final User localUser = this.userService.getLocalUser(this.auth.getUser(session()));
        return ok(comandcenter.render(this.auth, localUser));
    }
}
