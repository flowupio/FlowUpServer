package controllers.admin;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import controllers.Secured;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

@Security.Authenticated(Secured.class)
@Restrict(@Group("admin"))
public class AdminController extends Controller {

    public Result index() {
        return ok(views.html.admin.index.render());
    }
}
