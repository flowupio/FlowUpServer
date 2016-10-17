package controllers;

import com.feth.play.module.pa.PlayAuthenticate;
import models.User;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.commandcenter;

import javax.inject.Inject;

@Security.Authenticated(Secured.class)
public class CommandCenterController extends Controller {

    private final PlayAuthenticate auth;


    @Inject
    public CommandCenterController(final PlayAuthenticate auth) {
        this.auth = auth;
    }

    public Result index() {
        final User localUser =  User.findByAuthUserIdentity(this.auth.getUser(session()));
        return ok(commandcenter.render(this.auth, localUser));
    }
}
