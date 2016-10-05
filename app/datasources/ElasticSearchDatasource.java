package datasources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.Configuration;
import play.Logger;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import usecases.MetricsDatasource;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.Instant;
import java.util.concurrent.CompletionStage;

public class ElasticSearchDatasource implements MetricsDatasource {

    private final WSClient ws;
    private final Configuration elasticsearchConf;

    @Inject
    public ElasticSearchDatasource(WSClient ws, @Named("elasticsearch") Configuration elasticsearchConf) {
        this.ws = ws;
        this.elasticsearchConf = elasticsearchConf;
    }

    @Override
    public CompletionStage<WSResponse> writeFakeCounter() {
        ObjectNode actionAndMetadataJson = Json.newObject();
        actionAndMetadataJson.putObject("index")
                .put("_index", "statsd-test_counter")
                .put("_type", "counter");

        JsonNode OptionalSourceJson = Json.newObject()
                .put("val", 123)
                .put("@timestamp", Instant.now().toEpochMilli());

        String content = actionAndMetadataJson + "\n" + OptionalSourceJson + "\n";

        Logger.debug(content);

        return ws.url(getElasticUrl()).setContentType("application/x-www-form-urlencoded")
                .post(content);
    }

    private String getElasticUrl() {
        String host = elasticsearchConf.getString("host");
        String bulkEndpoint = elasticsearchConf.getString("bulk_endpoint");

        return "http://" + host + bulkEndpoint;
    }
}
