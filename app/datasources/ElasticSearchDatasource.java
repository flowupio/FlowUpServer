package datasources;

import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.F;
import play.libs.Json;
import usecases.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class ElasticSearchDatasource implements MetricsDatasource {

    private static final String STATSD = "statsd-";
    public static final String FLOWUP = "flowup-";
    private final ElasticsearchClient elasticsearchClient;

    @Inject
    public ElasticSearchDatasource(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    @Override
    public CompletionStage<InsertResult> writeDataPoints(Report report) {
        List<IndexRequest> indexRequestList = new ArrayList<>();
        populateLegacyIndexRequest(report.getMetrics(), indexRequestList);
        populateIndexRequest(report, indexRequestList);

        return elasticsearchClient.postBulk(indexRequestList).thenApply(this::processResponse);
    }

    private void populateLegacyIndexRequest(List<Metric> metrics, List<IndexRequest> indexRequestList) {
        metrics.forEach(metric -> {

            metric.getDataPoints().forEach(datapoint -> {
                IndexRequest indexRequest = new IndexRequest(STATSD + metric.getName(), "counter");

                ObjectNode source = mapSource(datapoint);

                indexRequest.setSource(source);
                indexRequestList.add(indexRequest);
            });
        });
    }

    private ObjectNode mapSource(DataPoint datapoint) {
        ObjectNode source = Json.newObject()
                .put("@timestamp", datapoint.getTimestamp().getTime());


        for (F.Tuple<String, Value> measurement : datapoint.getMeasurements()) {
            if (measurement._2 != null) {
                if (measurement._2 instanceof BasicValue) {
                    BasicValue basicValue = (BasicValue) measurement._2;
                    source.put(measurement._1, basicValue.getValue());
                } else if (measurement._2 instanceof StatisticalValue) {
                    StatisticalValue basicValue = (StatisticalValue) measurement._2;
                    ObjectNode statisticalValue = Json.newObject()
                            .put("count", basicValue.getCount())
                            .put("min", basicValue.getMin())
                            .put("max", basicValue.getMax())
                            .put("mean", basicValue.getMean())
                            .put("median", basicValue.getMedian())
                            .put("standardDev", basicValue.getStandardDev())
                            .put("p1", basicValue.getP1())
                            .put("p2", basicValue.getP2())
                            .put("p5", basicValue.getP5())
                            .put("p10", basicValue.getP10())
                            .put("p90", basicValue.getP90())
                            .put("p95", basicValue.getP95())
                            .put("p98", basicValue.getP98())
                            .put("p99", basicValue.getP99());
                    source.set(measurement._1, statisticalValue);
                }
            }
        }

        for (F.Tuple<String, String> tag : datapoint.getTags()) {
            source.put(tag._1, tag._2);
        }
        return source;
    }

    private void populateIndexRequest(Report report, List<IndexRequest> indexRequestList) {
        report.getMetrics().forEach(metric -> {

            metric.getDataPoints().forEach(datapoint -> {
                IndexRequest indexRequest = new IndexRequest(FLOWUP + report.getAppPackage(), metric.getName());

                ObjectNode source = mapSource(datapoint);

                indexRequest.setSource(source);
                indexRequestList.add(indexRequest);
            });
        });
    }

    private InsertResult processResponse(BulkResponse bulkResponse) {
        List<InsertResult.MetricResult> items = new ArrayList<>();
        for (BulkItemResponse item : bulkResponse.getItems()) {
            String name = item.getIndex().replace(STATSD, "");
            int successful = item.getResponse().getShardInfo().getSuccessful();
            items.add(new InsertResult.MetricResult(name, successful));
        }

        return new InsertResult(bulkResponse.isError(), items);
    }
}
