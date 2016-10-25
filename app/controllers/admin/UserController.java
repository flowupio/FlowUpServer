package controllers.admin;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Transaction;
import models.User;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;

import javax.inject.Inject;
import javax.persistence.PersistenceException;
import java.util.UUID;

public class UserController extends Controller {

    private FormFactory formFactory;

    @Inject
    public UserController(FormFactory formFactory) {
        this.formFactory = formFactory;
    }

    /**
     * This result directly redirect to application home.
     */
    public Result GO_HOME = Results.redirect(
            controllers.admin.routes.UserController.list(0, "name", "asc", "")
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
                views.html.admin.user.list.render(
                        User.page(page, 10, sortBy, order, filter),
                        sortBy, order, filter
                )
        );
    }

    /**
     * Display the 'edit form' of a existing Computer.
     *
     * @param id Id of the computer to edit
     */
    public Result edit(String id) {
        UUID uuid = UUID.fromString(id);
        Form<User> userForm = formFactory.form(User.class).fill(
                User.find.byId(uuid)
        );
        return ok(
                views.html.admin.user.editForm.render(id, userForm)
        );
    }

    /**
     * Handle the 'edit form' submission
     *
     * @param id Id of the user to edit
     */
    public Result update(String id) throws PersistenceException {
        UUID uuid = UUID.fromString(id);
        Form<User> userForm = formFactory.form(User.class).bindFromRequest();
        if(userForm.hasErrors()) {
            return badRequest(views.html.admin.user.editForm.render(id, userForm));
        }

        Transaction txn = Ebean.beginTransaction();
        try {
            User savedUser = User.find.byId(uuid);
            if (savedUser != null) {
                User newUserData = userForm.get();
                savedUser.setEmail(newUserData.getEmail());
                savedUser.setName(newUserData.getName());
                savedUser.setActive(newUserData.isActive());
                savedUser.setEmailValidated(newUserData.isEmailValidated());

                savedUser.update();
                flash("success", "Computer " + userForm.get().getName() + " has been updated");
                txn.commit();
            }
        } finally {
            txn.end();
        }

        return GO_HOME;
    }

    /**
     * Display the 'new user form'.
     */
    public Result create() {
        Form<User> userForm = formFactory.form(User.class);
        return ok(
                views.html.admin.user.createForm.render(userForm)
        );
    }

    /**
     * Handle the 'new user form' submission
     */
    public Result save() {
        Form<User> userForm = formFactory.form(User.class).bindFromRequest();
        if (userForm.hasErrors()) {
            return badRequest(views.html.admin.user.createForm.render(userForm));
        }
        userForm.get().save();
        flash("success", "User " + userForm.get().getName() + " has been created");
        return GO_HOME;
    }

    /**
     * Handle user deletion
     */
    public Result delete(String id) {
        UUID uuid = UUID.fromString(id);
        User.find.ref(uuid).delete();
        flash("success", "Computer has been deleted");
        return GO_HOME;
    }
}
