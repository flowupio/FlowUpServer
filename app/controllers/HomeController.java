package controllers;

import com.feth.play.module.pa.PlayAuthenticate;
import datasources.grafana.GrafanaProxy;
import play.mvc.Controller;
import play.mvc.Http.Cookie;
import play.mvc.Result;
import views.html.login;

import javax.inject.Inject;

import static com.feth.play.module.pa.controllers.AuthenticateBase.noCache;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    static final String FLASH_MESSAGE_KEY = "message";
    static final String FLASH_ERROR_KEY = "error";
    private final PlayAuthenticate auth;
    private final GrafanaProxy grafanaProxy;

    @Inject
    public HomeController(PlayAuthenticate auth, GrafanaProxy grafanaProxy) {
        this.auth = auth;
        this.grafanaProxy = grafanaProxy;
    }

    public Result index() {
        if (isUserLoggedIn()) {
            return redirect(routes.CommandCenterController.index());
        } else {
            return redirect(routes.HomeController.login());
        }
    }

    public Result health() {
        return ok();
    }

    public Result login() {
        if (isUserLoggedIn()) {
            return redirect(routes.CommandCenterController.index());
        } else {
            return ok(login.render(auth));
        }
    }

    public Result logout() {
        noCache(response());
        return this.auth.logout(session())
                .withCookies(grafanaProxy.logoutCookies().toArray(new Cookie[0]));
    }

    public Result oAuthDenied(final String providerKey) {
        noCache(response());
        flash(FLASH_ERROR_KEY,
                "You need to accept the OAuth connection in order to use this website!");
        return redirect(routes.HomeController.login());
    }

    private boolean isUserLoggedIn() {
        return auth.isLoggedIn(session());
    }
}