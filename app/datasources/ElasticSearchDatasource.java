package datasources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import usecases.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class ElasticSearchDatasource implements MetricsDatasource {

    private static final String STATSD = "statsd-";
    private final ElasticsearchClient elasticsearchClient;

    @Inject
    public ElasticSearchDatasource(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    @Override
    public CompletionStage<InsertResult> writeDataPoints(List<Metric> metrics) {
        List<JsonNode> content = new ArrayList<>();
        metrics.forEach(metric -> {
            ObjectNode actionAndMetadataJson = Json.newObject();
            actionAndMetadataJson.putObject("index")
                    .put("_index", STATSD + metric.getName())
                    .put("_type", "counter");

            metric.getDataPoints().forEach(datapoint -> {

                ObjectNode sourceJson = Json.newObject()
                        .put("@timestamp", datapoint.getTimestamp().getTime());

                datapoint.getMeasurements().stream().filter(measurement -> measurement._2 != null).forEach(measurement -> {
                    if (measurement._2 instanceof BasicValue) {
                        BasicValue basicValue = (BasicValue) measurement._2;
                        sourceJson.put(measurement._1, basicValue.getValue());
                    } else if (measurement._2 instanceof StatisticalValue) {
                        StatisticalValue basicValue = (StatisticalValue) measurement._2;
                        JsonNode statisticalValueJson = Json.newObject()
                                .put("count", basicValue.getCount())
                                .put("min", basicValue.getMin())
                                .put("max", basicValue.getMax())
                                .put("mean", basicValue.getMean())
                                .put("median", basicValue.getMedian())
                                .put("standardDev", basicValue.getStandardDev())
                                .put("p5", basicValue.getP5())
                                .put("p10", basicValue.getP10())
                                .put("p15", basicValue.getP15())
                                .put("p10", basicValue.getP20())
                                .put("p25", basicValue.getP25())
                                .put("p30", basicValue.getP30())
                                .put("p40", basicValue.getP40())
                                .put("p50", basicValue.getP50())
                                .put("p60", basicValue.getP60())
                                .put("p70", basicValue.getP70())
                                .put("p80", basicValue.getP80())
                                .put("p90", basicValue.getP90())
                                .put("p95", basicValue.getP95())
                                .put("p98", basicValue.getP98())
                                .put("p99", basicValue.getP99());

                        sourceJson.put(measurement._1, statisticalValueJson);
                    }
                });

                datapoint.getTags().forEach(tag -> {
                    sourceJson.put(tag._1, tag._2);
                });

                content.add(actionAndMetadataJson);
                content.add(sourceJson);
            });
        });

        return elasticsearchClient.postBulk(content).thenApply(this::processResponse);
    }

    private InsertResult processResponse(JsonNode jsonNode) {
        List<InsertResult.MetricResult> items = new ArrayList<>();
        jsonNode.get("items").forEach(item -> {
            JsonNode createNode = item.get("create");
            String name = createNode.get("_index").asText().replace(STATSD, "");
            items.add(new InsertResult.MetricResult(name, createNode.get("_shards").get("successful").asLong()));
        });

        return new InsertResult(jsonNode.get("errors").asBoolean(), items);
    }
}
