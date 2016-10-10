package datasources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.libs.Json;
import usecases.BasicValue;
import usecases.DataPoint;
import usecases.MetricsDatasource;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class ElasticSearchDatasource implements MetricsDatasource {

    private final ElasticsearchClient elasticsearchClient;

    @Inject
    public ElasticSearchDatasource(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    @Override
    public CompletionStage<JsonNode> writeDataPoints(List<DataPoint> dataPoints) {
        ObjectNode actionAndMetadataJson = Json.newObject();

        List<String> content = new ArrayList<>();

        actionAndMetadataJson.putObject("index")
                .put("_index", "statsd-network_data")
                .put("_type", "counter");

        dataPoints.forEach(datapoint -> {

            ObjectNode sourceJson = Json.newObject()
                    .put("@timestamp", datapoint.getTimestamp().getTime());

            datapoint.getMeasurements().forEach(measurement -> {
                if (measurement._2 instanceof BasicValue) {
                    BasicValue basicValue = (BasicValue) measurement._2;
                    sourceJson.put(measurement._1, basicValue.getValue());
                }
            });

            datapoint.getTags().forEach(tag -> {
                sourceJson.put(tag._1, tag._2);
            });

            content.add(actionAndMetadataJson.toString());
            content.add(sourceJson.toString());

        });


        String bulkContent = StringUtils.join(content, "\n");

        Logger.debug(bulkContent);

        return elasticsearchClient.postBulk(bulkContent);
    }
}
