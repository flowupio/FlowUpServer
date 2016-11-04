package controllers;

import com.feth.play.module.pa.PlayAuthenticate;
import datasources.grafana.GrafanaProxy;
import models.ApiKey;
import models.User;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import views.html.commandcenter.home;


import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

@Security.Authenticated(Secured.class)
public class CommandCenterController extends Controller {

    private final PlayAuthenticate auth;
    private final GrafanaProxy grafanaProxy;


    @Inject
    public CommandCenterController(PlayAuthenticate auth, GrafanaProxy grafanaProxy) {
        this.auth = auth;
        this.grafanaProxy = grafanaProxy;
    }

    public Result index() {
        final User localUser =  User.findByAuthUserIdentity(this.auth.getUser(session()));
        ApiKey apiKey = localUser.getOrganizations().get(0).getApiKey();
        return ok(home.render(this.auth, localUser, apiKey));
    }

    public CompletionStage<Result> grafana() {
        final User localUser =  User.findByAuthUserIdentity(this.auth.getUser(session()));

        return grafanaProxy.retreiveSessionCookies(localUser).thenApply(cookies -> {
            return redirect(grafanaProxy.getHomeUrl()).withCookies(cookies.toArray(new Http.Cookie[cookies.size()]));
        });
    }
}
