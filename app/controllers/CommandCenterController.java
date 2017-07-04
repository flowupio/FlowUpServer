package controllers;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;
import com.spotify.futures.CompletableFutures;
import datasources.grafana.GrafanaProxy;
import installationscounter.ui.UpgradeBillingPlanInfo;
import installationscounter.usecase.GetInstallationsCounterByApiKey;
import models.*;
import play.Configuration;
import play.Logger;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import usecases.*;
import usecases.models.KeyStatCard;
import views.html.commandcenter.application;
import views.html.commandcenter.billing;
import views.html.commandcenter.gettingStartedIOS;
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
    private final GetLatestSDKVersionName getLatestSDKVersionName;
    private final GetBillingInformation getBillingInformation;
    private final CreateSubscription createSubscription;
    private final GetInstallationsCounterByApiKey getInstallations;
    private final String stripePublicApiKey;
    private final FormFactory formFactory;


    @Inject
    public CommandCenterController(PlayAuthenticate auth, GrafanaProxy grafanaProxy, GetUserByAuthUserIdentity getUserByAuthUserIdentity,
                                   GetPrimaryOrganization getPrimaryOrganization, GetApplicationById getApplicationById,
                                   GetKeyMetrics getKeyMetrics, GetLatestSDKVersionName getLatestSDKVersionName,
                                   GetBillingInformation getBillingInformation, @Named("stripe") Configuration configuration,
                                   CreateSubscription createSubscription, GetInstallationsCounterByApiKey getInstallations,
                                   FormFactory formFactory) {
        this.auth = auth;
        this.grafanaProxy = grafanaProxy;
        this.getUserByAuthUserIdentity = getUserByAuthUserIdentity;
        this.getPrimaryOrganization = getPrimaryOrganization;
        this.getApplicationById = getApplicationById;
        this.getKeyMetrics = getKeyMetrics;
        this.getLatestSDKVersionName = getLatestSDKVersionName;
        this.getBillingInformation = getBillingInformation;
        this.stripePublicApiKey = configuration.getString("public_api_key");
        this.createSubscription = createSubscription;
        this.getInstallations = getInstallations;
        this.formFactory = formFactory;
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

    public CompletionStage<Result> gettingStartedAndroid() {
        return gettingStarted(Platform.ANDROID);
    }

    public CompletionStage<Result> gettingStartedIOS() {
        return gettingStarted(Platform.IOS);
    }

    public CompletionStage<Result> gettingStarted() {
        return gettingStartedAndroid();
    }

    public CompletionStage<Result> application(String applicationUUID) {
        AuthUser authUser = this.auth.getUser(session());
        User user = getUserByAuthUserIdentity.execute(authUser);
        CompletionStage<Organization> organizationCompletionStage = getPrimaryOrganization.execute(user);

        Application applicationModel = getApplicationById.execute(UUID.fromString(applicationUUID));
        CompletionStage<List<KeyStatCard>> keyMetricsCompletionStage = getKeyMetrics.execute(applicationModel);

        ApiKey apiKey = applicationModel.getOrganization().getApiKey();
        CompletionStage<Long> installationsCompletionStage = getInstallations.execute(apiKey.getValue());
        return CompletableFutures.combine(organizationCompletionStage, keyMetricsCompletionStage, installationsCompletionStage,
                (organization, statCards, installationsCounter) -> {
                    UpgradeBillingPlanInfo upgradeBillingPlanInfo = new UpgradeBillingPlanInfo(apiKey.getNumberOfAllowedUUIDs(), installationsCounter);
                    return ok(application.render(user, applicationModel, organization.getApplications(), statCards, upgradeBillingPlanInfo));
                });
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
                                ok(billing.render(user, organization.getApplications(), stripePublicApiKey, billingInformation))));
    }

    public CompletionStage<Result> createSubscription() {
        CreateSubscriptionForm form = formFactory.form(CreateSubscriptionForm.class).bindFromRequest().get();

        Logger.debug("Creating subscription: " + form);
        return createSubscription.createSubscription(form.getEmail(), form.getCountry(), form.getBuyerIp(),
                form.getToken(), form.getPlan(), Integer.parseInt(form.getQuantity())).thenApply(success -> {
            if (success) {
                return ok();
            } else {
                return internalServerError();
            }
        });
    }

    public Result grafana() {
        return redirect(routes.CommandCenterController.dashboards());
    }

    private CompletionStage<Result> gettingStarted(final Platform platform) {
        AuthUser authUser = auth.getUser(session());
        User user = getUserByAuthUserIdentity.execute(authUser);
        CompletionStage<Organization> organizationFuture = getPrimaryOrganization.execute(user);
        CompletionStage<String> sdkVersionFuture = getLatestSDKVersionName.execute(platform);
        return CompletableFutures.combine(organizationFuture, sdkVersionFuture, (organization, sdkVersionName) -> {
                    switch (platform) {
                        case IOS:
                            return ok(gettingStartedIOS.render(auth, user, organization.getApiKey(), organization.getApplications(), sdkVersionName, !organization.hasApplications()));
                        case ANDROID:
                        default:
                            return ok(home.render(auth, user, organization.getApiKey(), organization.getApplications(), sdkVersionName, !organization.hasApplications()));
                    }
                }
        );
    }
}
