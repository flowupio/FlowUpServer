package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import usecases.InsertDataPoints;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    @Inject
    InsertDataPoints insertDataPoints;

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public CompletionStage<Result> index() {
        return insertDataPoints.execute().thenApply(response ->
                ok("Metric Inserted \n" + "elastic response: " + response.getBody())
        );
    }
}
