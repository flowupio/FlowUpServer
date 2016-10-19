package datasources;

import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.xcontent.XContentBuilder;
import play.Logger;
import play.libs.F;
import usecases.*;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class ElasticSearchDatasource implements MetricsDatasource {

    private static final String STATSD = "statsd-";
    private final ElasticsearchClient elasticsearchClient;

    @Inject
    public ElasticSearchDatasource(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    @Override
    public CompletionStage<InsertResult> writeDataPoints(List<Metric> metrics) {
        List<IndexRequest> indexRequestList = new ArrayList<>();
        metrics.forEach(metric -> {

            metric.getDataPoints().forEach(datapoint -> {
                IndexRequest indexRequest = new IndexRequest(STATSD + metric.getName(), "counter");

                try {
                    XContentBuilder builder = jsonBuilder().startObject()
                            .field("@timestamp", datapoint.getTimestamp().getTime());


                    for (F.Tuple<String, Value> measurement : datapoint.getMeasurements()) {
                        if (measurement._2 != null) {
                            if (measurement._2 instanceof BasicValue) {
                                BasicValue basicValue = (BasicValue) measurement._2;
                                builder.field(measurement._1, basicValue.getValue());
                            } else if (measurement._2 instanceof StatisticalValue) {
                                StatisticalValue basicValue = (StatisticalValue) measurement._2;
                                builder.startObject(measurement._1)
                                        .field("count", basicValue.getCount())
                                        .field("min", basicValue.getMin())
                                        .field("max", basicValue.getMax())
                                        .field("mean", basicValue.getMean())
                                        .field("median", basicValue.getMedian())
                                        .field("standardDev", basicValue.getStandardDev())
                                        .field("p1", basicValue.getP1())
                                        .field("p2", basicValue.getP2())
                                        .field("p5", basicValue.getP5())
                                        .field("p10", basicValue.getP10())
                                        .field("p80", basicValue.getP80())
                                        .field("p95", basicValue.getP95())
                                        .field("p98", basicValue.getP98())
                                        .field("p99", basicValue.getP99());
                                builder.endObject();
                            }
                        }
                    }

                    for (F.Tuple<String, String> tag : datapoint.getTags()) {
                        builder.field(tag._1, tag._2);
                    }

                    builder.endObject();
                    indexRequest.source(builder);
                    indexRequestList.add(indexRequest);
                } catch (IOException e) {
                    Logger.debug(e.getMessage());
                }

            });
        });

        return elasticsearchClient.postBulk(indexRequestList).thenApply(this::processResponse);
    }

    private InsertResult processResponse(BulkResponse bulkResponse) {
        List<InsertResult.MetricResult> items = new ArrayList<>();
        for (BulkItemResponse item : bulkResponse.getItems()) {
            String name = item.getIndex().replace(STATSD, "");
            int successful = item.getResponse().getShardInfo().getSuccessful();
            items.add(new InsertResult.MetricResult(name, successful));
        }

        return new InsertResult(bulkResponse.hasFailures(), items);
    }
}
