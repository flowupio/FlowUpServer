package datasources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.Logger;
import play.libs.Json;
import usecases.MetricsDatasource;

import javax.inject.Inject;
import java.time.Instant;
import java.util.concurrent.CompletionStage;

public class ElasticSearchDatasource implements MetricsDatasource {

    private final ElasticsearchClient elasticsearchClient;

    @Inject
    public ElasticSearchDatasource(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    @Override
    public CompletionStage<JsonNode> writeFakeCounter() {
        ObjectNode actionAndMetadataJson = Json.newObject();
        actionAndMetadataJson.putObject("index")
                .put("_index", "statsd-test_counter")
                .put("_type", "counter");

        JsonNode OptionalSourceJson = Json.newObject()
                .put("val", 123)
                .put("@timestamp", Instant.now().toEpochMilli());

        String content = actionAndMetadataJson + "\n" + OptionalSourceJson + "\n";

        Logger.debug(content);

        return elasticsearchClient.postBulk(content);
    }
}
