package controllers;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;
import com.spotify.futures.CompletableFutures;
import datasources.grafana.GrafanaProxy;
import models.Application;
import models.Organization;
import models.User;
import play.Configuration;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import usecases.*;
import usecases.models.KeyStatCard;
import views.html.commandcenter.application;
import views.html.commandcenter.billing;
import views.html.commandcenter.home;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

@Security.Authenticated(Secured.class)
public class CommandCenterController extends Controller {

    private final PlayAuthenticate auth;
    private final GrafanaProxy grafanaProxy;
    private final GetUserByAuthUserIdentity getUserByAuthUserIdentity;
    private final GetPrimaryOrganization getPrimaryOrganization;
    private final GetApplicationById getApplicationById;
    private final GetKeyMetrics getKeyMetrics;
    private final GetLatestAndroidSDKVersionName getLatestAndroidSDKVersionName;
    private final GetBillingInformation getBillingInformation;
    private final String taxamoPublicApiKey;


    @Inject
    public CommandCenterController(PlayAuthenticate auth, GrafanaProxy grafanaProxy, GetUserByAuthUserIdentity getUserByAuthUserIdentity,
                                   GetPrimaryOrganization getPrimaryOrganization, GetApplicationById getApplicationById,
                                   GetKeyMetrics getKeyMetrics, GetLatestAndroidSDKVersionName getLatestAndroidSDKVersionName,
                                   GetBillingInformation getBillingInformation, @Named("taxamo") Configuration configuration) {
        this.auth = auth;
        this.grafanaProxy = grafanaProxy;
        this.getUserByAuthUserIdentity = getUserByAuthUserIdentity;
        this.getPrimaryOrganization = getPrimaryOrganization;
        this.getApplicationById = getApplicationById;
        this.getKeyMetrics = getKeyMetrics;
        this.getLatestAndroidSDKVersionName = getLatestAndroidSDKVersionName;
        this.getBillingInformation = getBillingInformation;
        this.taxamoPublicApiKey = configuration.getString("public_api_key");
    }

    public CompletionStage<Result> index() {
        AuthUser authUser = auth.getUser(session());
        User user = getUserByAuthUserIdentity.execute(authUser);
        return getPrimaryOrganization.execute(user)
                .thenApply(organization -> {
                    if (organization.getApplications() == null || organization.getApplications().isEmpty()) {
                        return redirect(routes.CommandCenterController.gettingStarted());
                    } else {
                        Application application = organization.getApplications().get(0);
                        return redirect(routes.CommandCenterController.application(application.getId().toString()));
                    }
                });
    }

    public CompletionStage<Result> gettingStarted() {
        AuthUser authUser = auth.getUser(session());
        User user = getUserByAuthUserIdentity.execute(authUser);
        CompletionStage<Organization> organizationFuture = getPrimaryOrganization.execute(user);
        CompletionStage<String> sdkVersionFuture = getLatestAndroidSDKVersionName.execute();
        return CompletableFutures.combine(organizationFuture, sdkVersionFuture, (organization, sdkVersionName) ->
                ok(home.render(auth, user, organization.getApiKey(), organization.getApplications(), sdkVersionName, !organization.hasApplications()))
        );
    }

    public CompletionStage<Result> application(String applicationUUID) {
        AuthUser authUser = this.auth.getUser(session());
        User user = getUserByAuthUserIdentity.execute(authUser);
        CompletionStage<Organization> organizationCompletionStage = getPrimaryOrganization.execute(user);

        Application applicationModel = getApplicationById.execute(UUID.fromString(applicationUUID));
        CompletionStage<List<KeyStatCard>> keyMetricsCompletionStage = getKeyMetrics.execute(applicationModel);

        return CompletableFutures.combine(organizationCompletionStage, keyMetricsCompletionStage, (organization, statCards) ->
                ok(application.render(user, applicationModel, organization.getApplications(), statCards)));
    }

    public CompletionStage<Result> dashboards() {
        final User localUser = User.findByAuthUserIdentity(this.auth.getUser(session()));

        return grafanaProxy.retreiveSessionCookies(localUser).thenApply(cookies ->
                redirect(grafanaProxy.getHomeUrl()).withCookies(cookies.toArray(new Http.Cookie[cookies.size()])));
    }

    public CompletionStage<Result> billing() {
        AuthUser authUser = auth.getUser(session());
        User user = getUserByAuthUserIdentity.execute(authUser);
        return getPrimaryOrganization.execute(user)
                .thenCompose(organization ->
                        getBillingInformation.execute(organization).thenApply(billingInformation ->
                                ok(billing.render(user, organization.getApplications(), organization.getBillingId(), taxamoPublicApiKey, billingInformation))));
    }

    public Result grafana() {
        return redirect(routes.CommandCenterController.dashboards());
    }
}
