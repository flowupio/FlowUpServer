package controllers;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;
import com.spotify.futures.CompletableFutures;
import datasources.grafana.GrafanaProxy;
import lombok.Data;
import models.Application;
import models.Organization;
import models.User;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import usecases.GetApplicationById;
import usecases.GetFramePerSecond;
import usecases.GetPrimaryOrganization;
import usecases.GetUserByAuthUserIdentity;
import usecases.models.StatCard;
import views.html.commandcenter.home;
import views.html.commandcenter.application;


import javax.inject.Inject;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static java.util.Arrays.asList;

@Security.Authenticated(Secured.class)
public class CommandCenterController extends Controller {

    private final PlayAuthenticate auth;
    private final GrafanaProxy grafanaProxy;
    private final GetUserByAuthUserIdentity getUserByAuthUserIdentity;
    private final GetPrimaryOrganization getPrimaryOrganization;
    private final GetApplicationById getApplicationById;
    private final GetFramePerSecond getFramePerSecond;


    @Inject
    public CommandCenterController(PlayAuthenticate auth, GrafanaProxy grafanaProxy, GetUserByAuthUserIdentity getUserByAuthUserIdentity, GetPrimaryOrganization getPrimaryOrganization, GetApplicationById getApplicationById, GetFramePerSecond getFramePerSecond) {
        this.auth = auth;
        this.grafanaProxy = grafanaProxy;
        this.getUserByAuthUserIdentity = getUserByAuthUserIdentity;
        this.getPrimaryOrganization = getPrimaryOrganization;
        this.getApplicationById = getApplicationById;
        this.getFramePerSecond = getFramePerSecond;
    }

    public Result index() {
        AuthUser authUser = this.auth.getUser(session());
        User user = getUserByAuthUserIdentity.execute(authUser);
        Organization organization = getPrimaryOrganization.execute(user);
        return ok(home.render(this.auth, user, organization.getApiKey(), organization.getApplications()));
    }

    public CompletionStage<Result> application(String applicationUUID) {
        AuthUser authUser = this.auth.getUser(session());
        User user = getUserByAuthUserIdentity.execute(authUser);
        Organization organization = getPrimaryOrganization.execute(user);

        Application applicationModel = getApplicationById.execute(UUID.fromString(applicationUUID));
        CompletableFuture<StatCard> framePerSecondCompletionStage = getFramePerSecond.execute(applicationModel).toCompletableFuture();
        CompletableFuture<StatCard> internalStorageUsageCompletionStage = getFramePerSecond.execute(applicationModel).toCompletableFuture();
        CompletableFuture<StatCard> cpuUsageCompletionStage = getFramePerSecond.execute(applicationModel).toCompletableFuture();
        CompletableFuture<StatCard> memoryUsageCompletionStage = getFramePerSecond.execute(applicationModel).toCompletableFuture();

        List<CompletableFuture<StatCard>> futures = asList(framePerSecondCompletionStage, internalStorageUsageCompletionStage, cpuUsageCompletionStage, memoryUsageCompletionStage);
        return CompletableFutures.allAsList(futures).thenApply(statCards ->  {
            return ok(application.render(user, applicationModel, organization.getApplications(), statCards));
        });
    }

    public CompletionStage<Result> grafana() {
        final User localUser =  User.findByAuthUserIdentity(this.auth.getUser(session()));

        return grafanaProxy.retreiveSessionCookies(localUser).thenApply(cookies -> {
            return redirect(grafanaProxy.getHomeUrl()).withCookies(cookies.toArray(new Http.Cookie[cookies.size()]));
        });
    }
}
