package controllers;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;
import com.spotify.futures.CompletableFutures;
import datasources.grafana.GrafanaProxy;
import models.Application;
import models.Organization;
import models.User;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import usecases.*;
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
    private final GetKeyMetrics getKeyMetrics;
    private final GetLatestAndroidSDKVersionName getLatestAndroidSDKVersionName;


    @Inject
    public CommandCenterController(PlayAuthenticate auth, GrafanaProxy grafanaProxy, GetUserByAuthUserIdentity getUserByAuthUserIdentity,
                                   GetPrimaryOrganization getPrimaryOrganization, GetApplicationById getApplicationById,
                                   GetKeyMetrics getKeyMetrics, GetLatestAndroidSDKVersionName getLatestAndroidSDKVersionName) {
        this.auth = auth;
        this.grafanaProxy = grafanaProxy;
        this.getUserByAuthUserIdentity = getUserByAuthUserIdentity;
        this.getPrimaryOrganization = getPrimaryOrganization;
        this.getApplicationById = getApplicationById;
        this.getKeyMetrics = getKeyMetrics;
        this.getLatestAndroidSDKVersionName = getLatestAndroidSDKVersionName;
    }

    public CompletionStage<Result> index() {
        AuthUser authUser = auth.getUser(session());
        User user = getUserByAuthUserIdentity.execute(authUser);
        CompletableFuture<Organization> organizationFuture = CompletableFuture.supplyAsync(() -> getPrimaryOrganization.execute(user));
        CompletableFuture<String> sdkVersionFuture = getLatestAndroidSDKVersionName.execute().toCompletableFuture();
        return CompletableFutures.combine(organizationFuture, sdkVersionFuture, (organization, sdkVersionName) ->
                ok(home.render(auth, user, organization.getApiKey(), organization.getApplications(), sdkVersionName))
        );

    }

    public CompletionStage<Result> application(String applicationUUID) {
        AuthUser authUser = this.auth.getUser(session());
        User user = getUserByAuthUserIdentity.execute(authUser);
        Organization organization = getPrimaryOrganization.execute(user);

        Application applicationModel = getApplicationById.execute(UUID.fromString(applicationUUID));

        return getKeyMetrics.execute(applicationModel).thenApply(statCards -> {
            return ok(application.render(user, applicationModel, organization.getApplications(), statCards));
        });
    }

    public CompletionStage<Result> grafana() {
        final User localUser = User.findByAuthUserIdentity(this.auth.getUser(session()));

        return grafanaProxy.retreiveSessionCookies(localUser).thenApply(cookies -> {
            return redirect(grafanaProxy.getHomeUrl()).withCookies(cookies.toArray(new Http.Cookie[cookies.size()]));
        });
    }
}
