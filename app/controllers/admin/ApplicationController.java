package controllers.admin;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import controllers.Secured;
import controllers.admin.form.ChangeMinAndroidSDKVersion;
import models.Application;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Security;
import usecases.UpdateMinAndroidSDKVersionSupported;

import javax.inject.Inject;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Security.Authenticated(Secured.class)
@Restrict(@Group("admin"))
public class ApplicationController extends Controller {

    private final FormFactory formFactory;
    private final HttpExecutionContext ec;
    private final UpdateMinAndroidSDKVersionSupported updateSdk;

    @Inject
    public ApplicationController(FormFactory formFactory, HttpExecutionContext ec, UpdateMinAndroidSDKVersionSupported updateSdk) {
        this.formFactory = formFactory;
        this.ec = ec;
        this.updateSdk = updateSdk;
    }

    /**
     * This result directly redirect to application home.
     */
    private Result GO_HOME = Results.redirect(
            controllers.admin.routes.ApplicationController.list(0, "app_package", "asc", "")
    );

    /**
     * Handle default path requests, redirect to computers list
     */
    public Result index() {
        return GO_HOME;
    }

    /**
     * Display the paginated list of computers.
     *
     * @param page   Current page number (starts from 0)
     * @param sortBy Column to be sorted
     * @param order  Sort order (either asc or desc)
     * @param filter Filter applied on computer names
     */
    public Result list(int page, String sortBy, String order, String filter) {
        return ok(
                views.html.admin.application.list.render(
                        Application.page(page, 10, sortBy, order, filter),
                        sortBy, order, filter
                )
        );
    }

    public Result delete(String id) {
        UUID uuid = UUID.fromString(id);
        Application.find.ref(uuid).delete();
        flash("success", "Application has been deleted");
        return GO_HOME;
    }

    public CompletionStage<Result> updateMinAndroidSDKVersionSupported(String apiKeyId) {
        ChangeMinAndroidSDKVersion form = formFactory.form(ChangeMinAndroidSDKVersion.class).bindFromRequest().get();
        return CompletableFuture.supplyAsync(() -> {
            String version = form.getMinAndroidSdkSupported().trim();
            return updateSdk.execute(apiKeyId, version);
        }).thenApply(apiKey -> GO_HOME);
    }
}
