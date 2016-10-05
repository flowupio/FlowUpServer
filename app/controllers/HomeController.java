package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.Logger;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.time.Instant;
import java.util.concurrent.CompletionStage;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    @Inject
    WSClient ws;

    private static String elasticUrl = "http://localhost:9200/_bulk";

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    public CompletionStage<Result> index() {
        ObjectNode actionAndMetadataJson = Json.newObject();
        actionAndMetadataJson.putObject("index")
                .put("_index", "statsd-test_counter")
                .put("_type", "counter");

        JsonNode OptionalSourceJson = Json.newObject()
                .put("val", 123)
                .put("@timestamp", Instant.now().toEpochMilli());

        String content = actionAndMetadataJson + "\n" + OptionalSourceJson + "\n";

        Logger.debug(content);



        return ws.url(elasticUrl).setContentType("application/x-www-form-urlencoded")
                .post(content).thenApply(response ->
                ok("elastic response: " + response.getBody())
        );
    }
}
