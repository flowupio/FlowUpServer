package datasources.elasticsearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Application;
import org.jetbrains.annotations.NotNull;
import play.libs.F;
import play.libs.Json;
import usecases.InsertResult;
import usecases.MetricsDatasource;
import usecases.SingleStatQuery;
import usecases.models.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class ElasticSearchDatasource implements MetricsDatasource {

    private static final String FLOWUP = "flowup";
    private static final String DELIMITER = "-";
    private final ElasticsearchClient elasticsearchClient;

    @Inject
    public ElasticSearchDatasource(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    @Override
    public CompletionStage<InsertResult> writeDataPoints(Report report, Application application) {
        List<IndexRequest> indexRequestList = new ArrayList<>();
        populateIndexRequest(report, indexRequestList, application);

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
                            .put("mean", basicValue.getMean())
                            .put("p10", basicValue.getP10())
                            .put("p90", basicValue.getP90());
                    source.set(measurement._1, statisticalValue);
                }
            }
        }

        for (F.Tuple<String, String> tag : datapoint.getTags()) {
            source.put(tag._1, tag._2);
        }
        return source;
    }

    private void populateIndexRequest(Report report, List<IndexRequest> indexRequestList, Application application) {
        report.getMetrics().forEach(metric -> {

            metric.getDataPoints().forEach(datapoint -> {
                IndexRequest indexRequest = new IndexRequest(indexName(report.getAppPackage(), application.getOrganization().getId().toString()), metric.getName());

                ObjectNode source = mapSource(datapoint);

                indexRequest.setSource(source);
                indexRequestList.add(indexRequest);
            });
        });
    }

    private InsertResult processBulkResponse(BulkResponse bulkResponse) {
        List<InsertResult.MetricResult> items = new ArrayList<>();
        for (BulkItemResponse item : bulkResponse.getItems()) {
            String name = item.getIndex().replace(FLOWUP + DELIMITER, "");
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

    public CompletionStage<LineChart> singleStat(SingleStatQuery singleStatQuery) {
        long gteEpochMillis = singleStatQuery.getFrom().toEpochMilli();
        long lteEpochMillis = singleStatQuery.getTo().toEpochMilli();

        SearchQuery searchQuery = prepareSearchQuery(singleStatQuery.getApplication(), gteEpochMillis, lteEpochMillis, singleStatQuery.getField(), singleStatQuery.getQueryStringValue());

        return elasticsearchClient.multiSearch(Collections.singletonList(searchQuery)).thenApply(this::processMSearchResponse);
    }

    @Override
    public CompletionStage<List<LineChart>> statGroupBy(SingleStatQuery singleStatQuery, String field) {
        long gteEpochMillis = singleStatQuery.getFrom().toEpochMilli();
        long lteEpochMillis = singleStatQuery.getTo().toEpochMilli();

        SearchQuery searchQuery = prepareSearchQuery(singleStatQuery.getApplication(), gteEpochMillis, lteEpochMillis, singleStatQuery.getField(), singleStatQuery.getQueryStringValue());

        return elasticsearchClient.multiSearch(Collections.singletonList(searchQuery)).thenApply(this::processMSearchGroupByResponse);
    }

    private LineChart processMSearchResponse(MSearchResponse mSearchResponse) {
        JsonNode aggregations = getFirstAggregations(mSearchResponse);
        if (aggregations != null) {
            return processLineChartAggregation(aggregations);
        }

        return new LineChart(Collections.emptyList(), Collections.emptyList());
    }

    private List<LineChart> processMSearchGroupByResponse(MSearchResponse mSearchResponse) {
        List<LineChart> lineCharts = new ArrayList<>();
        JsonNode aggregations = getFirstAggregations(mSearchResponse);
        if (aggregations != null) {
            for (JsonNode bucket : aggregations.get("3").get("buckets")) {
                LineChart lineChart = processLineChartAggregation(bucket);
                lineCharts.add(lineChart);
            }
        }
        return lineCharts;
    }

    private LineChart processLineChartAggregation(JsonNode aggregations) {
        List<String> keys = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        JsonNode jsonNode = aggregations.get("2");
        if (jsonNode != null) {
            for (JsonNode bucket : jsonNode.get("buckets")) {
                keys.add(bucket.get("key").asText());
                JsonNode value = bucket.get("1").get("value");
                values.add(value instanceof NullNode ? null : value.asDouble());
            }
        }
        return new LineChart(keys, values);
    }

    private JsonNode getFirstAggregations(MSearchResponse mSearchResponse) {
        if (mSearchResponse.getResponses().size() > 0) {
            SearchResponse searchResponse = mSearchResponse.getResponses().get(0);
            return searchResponse.getAggregations();
        }
        return null;
    }

    @NotNull
    private SearchQuery prepareSearchQuery(Application application, long gteEpochMillis, long lteEpochMillis, String field, String queryStringValue) {
        SearchQuery searchQuery = new SearchQuery();

        SearchIndex searchIndex = new SearchIndex();
        searchIndex.setIndex(indexName(application.getAppPackage(), application.getOrganization().getId().toString()));
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
        queryString.setQuery(queryStringValue);
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

        Aggregation aggsObject = new Aggregation();

        DateHistogramAggregation dateHistogram = new DateHistogramAggregation(
                "4m",
                "@timestamp",
                0,
                "epoch_millis",
                new ExtendedBounds(gteEpochMillis, lteEpochMillis));
        aggsObject.setDateHistogram(dateHistogram);

        Aggregation aggregation = new Aggregation();
        aggregation.setAvg(new AvgAggregation(field));
        aggsObject.setAggs(AggregationMap.singleton("1", aggregation));

        searchBody.setAggs(AggregationMap.singleton("2", aggsObject));
        searchQuery.setSearchBody(searchBody);
        return searchQuery;
    }

    @NotNull
    public static String indexName(String appPackage, String organizationId) {
        return String.join(DELIMITER, FLOWUP, organizationId, appPackage);
    }
}
