package datasources.elasticsearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Application;
import org.jetbrains.annotations.NotNull;
import play.libs.F;
import play.libs.Json;
import usecases.*;
import usecases.models.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
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
        populateIndexRequest(report, indexRequestList);

        return elasticsearchClient.postBulk(indexRequestList).thenApply(this::processBulkResponse);
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
                IndexRequest indexRequest = new IndexRequest(indexName(report.getAppPackage()), metric.getName());

                ObjectNode source = mapSource(datapoint);

                indexRequest.setSource(source);
                indexRequestList.add(indexRequest);
            });
        });
    }

    private InsertResult processBulkResponse(BulkResponse bulkResponse) {
        List<InsertResult.MetricResult> items = new ArrayList<>();
        for (BulkItemResponse item : bulkResponse.getItems()) {
            String name = item.getIndex().replace(STATSD, "");
            ActionWriteResponse.ShardInfo shardInfo = item.getResponse().getShardInfo();
            int successful;
            if (shardInfo != null) {
                successful = shardInfo.getSuccessful();
            } else {
                successful = 0;
            }
            items.add(new InsertResult.MetricResult(name, successful));
        }

        return new InsertResult(bulkResponse.isError(), bulkResponse.hasFailures(), items);
    }

    public CompletionStage<LineChart> singleStat(Application application) {
        long gteEpochMillis = 1478386800000L;
        long lteEpochMillis = 1478773156290L;
        String field = "FramesPerSecond.p10";

        SearchQuery searchQuery = prepareSearchQuery(application, gteEpochMillis, lteEpochMillis, field);

        return elasticsearchClient.multiSearch(Collections.singletonList(searchQuery)).thenApply(this::processMSearchResponse);
    }

    private LineChart processMSearchResponse(MSearchResponse mSearchResponse) {
        List<String> keys = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        for (SearchResponse searchResponse : mSearchResponse.getResponses()) {
            for (JsonNode bucket : searchResponse.getAggregations().get("2").get("buckets")) {
                keys.add(bucket.get("key_as_string").asText());
                values.add(bucket.get("key").asDouble());
            }
        }

        return new LineChart(keys, values);
    }

    @NotNull
    private SearchQuery prepareSearchQuery(Application application, long gteEpochMillis, long lteEpochMillis, String field) {
        SearchQuery searchQuery = new SearchQuery();

        SearchIndex searchIndex = new SearchIndex();
        searchIndex.setIndex(indexName(application.getAppPackage()));
        searchIndex.setIgnoreUnavailable(true);
        searchIndex.setSearchType("count");
        searchQuery.setSearchIndex(searchIndex);

        SearchBody searchBody = new SearchBody();
        searchBody.setSize(0);
        SearchBodyQuery query = new SearchBodyQuery();
        SearchBodyQueryFiltered filtered = new SearchBodyQueryFiltered();
        SearchBodyQueryFilteredQuery filteredQuery = new SearchBodyQueryFilteredQuery();
        QueryString queryString = new QueryString();
        queryString.setAnalyzeWildcard(true);
        queryString.setQuery("");
        filteredQuery.setQueryString(queryString);
        filtered.setQuery(filteredQuery);
        ObjectNode filter = Json.newObject();
        JsonNode range = Json.newObject()
                .set("range", Json.newObject()
                        .set("@timestamp", Json.newObject()
                                .put("gte", gteEpochMillis)
                                .put("lte", lteEpochMillis)
                                .put("format", "epoch_millis")));
        filter.set("bool", Json.newObject().set("must", Json.newArray().add(range)));
        filtered.setFilter(filter);
        query.setFiltered(filtered);
        searchBody.setQuery(query);
        ObjectNode aggsObject = Json.newObject();
        JsonNode dateHistogram = Json.newObject()
                .put("interval", "4m")
                .put("field", "@timestamp")
                .put("min_doc_count", 0)
                .put("format", "epoch_millis")
                .set("extended_bounds", Json.newObject().put("min", gteEpochMillis).put("max", lteEpochMillis));
        aggsObject.set("date_histogram", dateHistogram);
        JsonNode aggsNode = Json.newObject().set("1", Json.newObject().set("avg", Json.newObject().put("field", field)));
        aggsObject.set("aggs", aggsNode);
        JsonNode aggs = Json.newObject().set("2", aggsObject);
        searchBody.setAggs(aggs);
        searchQuery.setSearchBody(searchBody);
        return searchQuery;
    }

    @NotNull
    private String indexName(String appPackage) {
        return FLOWUP + appPackage;
    }
}
