package controllers;

import com.feth.play.module.pa.PlayAuthenticate;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.login;

import javax.inject.Inject;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    public static final String FLASH_MESSAGE_KEY = "message";
    public static final String FLASH_ERROR_KEY = "error";

    @Inject
    PlayAuthenticate auth;

    public Result index() {
        return ok("Welcome to FlowUp");
    }

    public Result login() {
        return ok(login.render(this.auth));
    }

    public Result oAuthDenied(final String providerKey) {
        com.feth.play.module.pa.controllers.Authenticate.noCache(response());
        flash(FLASH_ERROR_KEY,
                "You need to accept the OAuth connection in order to use this website!");
        return redirect(routes.HomeController.login());

    }
}