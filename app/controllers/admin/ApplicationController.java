package controllers.admin;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import controllers.Secured;
import models.Application;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Security;

import javax.inject.Inject;
import javax.persistence.PersistenceException;
import java.util.UUID;

@Security.Authenticated(Secured.class)
@Restrict(@Group("admin"))
public class ApplicationController extends Controller {

    private FormFactory formFactory;
    private final HttpExecutionContext ec;

    @Inject
    public ApplicationController(FormFactory formFactory, HttpExecutionContext ec) {
        this.formFactory = formFactory;
        this.ec = ec;
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

    public Result edit(String id) {
        UUID uuid = UUID.fromString(id);
        Form<Application> form = formFactory.form(Application.class).fill(
                Application.find.byId(uuid)
        );
        return ok(
                views.html.admin.application.editForm.render(id, form)
        );
    }

    public Result update(String id) throws PersistenceException {
        UUID uuid = UUID.fromString(id);
        Form<Application> form = formFactory.form(Application.class).bindFromRequest();
        if(form.hasErrors()) {
            return badRequest(views.html.admin.application.editForm.render(id, form));
        }

        Transaction txn = Ebean.beginTransaction();
        try {
            Application saved = Application.find.byId(uuid);
            if (saved != null) {
                Application newData = form.get();
                saved.setGrafanaOrgId(newData.getGrafanaOrgId());
                saved.setOrganization(newData.getOrganization());

                saved.update();
                flash("success", "Application " + form.get().getAppPackage() + " has been updated");
                txn.commit();
            }
        } finally {
            txn.end();
        }

        return GO_HOME;
    }

    public Result create() {
        Form<Application> form = formFactory.form(Application.class);
        return ok(
                views.html.admin.application.createForm.render(form)
        );
    }

    public Result save() {
        Form<Application> form = formFactory.form(Application.class).bindFromRequest();
        if (form.hasErrors()) {
            return badRequest(views.html.admin.application.createForm.render(form));
        }
        form.get().save();
        flash("success", "Application " + form.get().getAppPackage() + " has been created");
        return GO_HOME;
    }

    public Result delete(String id) {
        UUID uuid = UUID.fromString(id);
        Application.find.ref(uuid).delete();
        flash("success", "Application has been deleted");
        return GO_HOME;
    }
}
