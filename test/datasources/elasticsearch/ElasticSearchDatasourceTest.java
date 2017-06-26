package datasources.elasticsearch;

import com.fasterxml.jackson.databind.JsonNode;
import models.Application;
import models.Platform;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import play.libs.F;
import play.libs.Json;
import usecases.InsertResult;
import usecases.SingleStatQuery;
import usecases.models.*;
import utils.WithFlowUpApplication;
import utils.WithResources;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ElasticSearchDatasourceTest extends WithFlowUpApplication implements WithResources {

    private static final String ANY_FIELD = "AnyField";
    private static final int ANY_NUMBER_OF_INDEXES = 10;
    private static final int ANY_NUMBER_OF_DOCUMENTS = 20;

    @Override
    protected play.Application provideApplication() {
        return getGuiceApplicationBuilder()
                .build();
    }

    @Test
    public void parsingElasticSearchClientBulkResponse() throws Exception {
        ElasticSearchDatasource elasticSearchDatasource = givenElasticSearchDatasourceThatReturnTwoItems();
        Report report = givenAReportWithOneDataPoint();
        Application application = givenAnyApplication();

        CompletionStage<InsertResult> insertResultCompletionStage = elasticSearchDatasource.writeDataPoints(report, application);
        InsertResult insertResult = insertResultCompletionStage.toCompletableFuture().get();

        List<InsertResult.MetricResult> items = new ArrayList<>();
        items.add(new InsertResult.MetricResult("", 1));
        items.add(new InsertResult.MetricResult("", 1));
        assertEquals(new InsertResult(false, false, items), insertResult);
    }

    @Test
    public void parsingElasticSearchClientMSearchResponse() throws Exception {
        ElasticSearchDatasource elasticSearchDatasource = givenElasticSearchDatasourceThatReturnAggregation();
        Application application = givenAnyApplication();

        SingleStatQuery singleStatQuery = new SingleStatQuery(application, ANY_FIELD, new F.Tuple<>(Instant.now().minusSeconds(1L), Instant.now()));
        CompletionStage<LineChart> lineChartCompletionStage = elasticSearchDatasource.singleStat(singleStatQuery);
        LineChart lineChart = lineChartCompletionStage.toCompletableFuture().get();

        List<Double> values = Arrays.asList(49.714285714285715, 47.73076923076923, 48.714285714285715, 47.22727272727273, 46.083333333333336, 47.25);
        List<String> labels = Arrays.asList("1478866080000", "1478866320000", "1478866560000", "1478866800000", "1478867040000", "1478867280000");
        assertEquals(values, lineChart.getValues());
        assertEquals(labels, lineChart.getLabels());
    }

    @NotNull
    private Report givenAReportWithOneDataPoint() {
        String organizationIdentifier = "3e02e6b9-3a33-4113-ae78-7d37f11ca3bf";
        DataPoint dataPoint = new DataPoint(new Date(), Collections.singletonList(new F.Tuple<>("any_measurement", Value.toBasicValue(0))), Collections.singletonList(new F.Tuple<>("any_tag", "tag")));
        Metric anyMetric = new Metric("any_metric", Collections.singletonList(dataPoint));
        return new Report(organizationIdentifier, "io.flowup.app", Collections.singletonList(anyMetric), new Report.Metadata(false, false), Platform.ANDROID);
    }

    @NotNull
    private Report givenAnEmptyReport() {
        String organizationIdentifier = "3e02e6b9-3a33-4113-ae78-7d37f11ca3bf";
        DataPoint dataPoint = new DataPoint(new Date(), Collections.singletonList(new F.Tuple<>("any_measurement", Value.toBasicValue(0))), Collections.singletonList(new F.Tuple<>("any_tag", "tag")));
        Metric anyMetric = new Metric("any_metric", Collections.singletonList(dataPoint));
        return new Report(organizationIdentifier, "io.flowup.app", Collections.singletonList(anyMetric), new Report.Metadata(false, false), Platform.ANDROID);
    }

    @Test
    public void validateJsonBulkResponse() {
        try {
            JsonNode postBulkResult = Json.parse(getFile("elasticsearch/es_bulk_response.json"));
            Json.fromJson(postBulkResult, BulkResponse.class);
        } catch (RuntimeException exception) {
            fail(exception.getMessage());
        }
    }

    @Test
    public void parsingElasticSearchClientSimpleResponse() throws Exception {
        ElasticSearchDatasource elasticSearchDatasource = givenElasticSearchDatasourceThatReturnOneItem();
        Report report = givenAReportWithOneDataPoint();
        Application application = givenAnyApplication();

        CompletionStage<InsertResult> insertResultCompletionStage = elasticSearchDatasource.writeDataPoints(report, application);
        InsertResult insertResult = insertResultCompletionStage.toCompletableFuture().get();

        List<InsertResult.MetricResult> items = new ArrayList<>();
        items.add(new InsertResult.MetricResult("", 1));
        assertEquals(new InsertResult(false, false, items), insertResult);
    }

    @Test
    public void parsingElasticSearchClientError() throws Exception {
        ElasticSearchDatasource elasticSearchDatasource = givenElasticSearchDatasourceThatReturnError();
        Report report = givenAnEmptyReport();
        Application application = givenAnyApplication();

        CompletionStage<InsertResult> insertResultCompletionStage = elasticSearchDatasource.writeDataPoints(report, application);
        InsertResult insertResult = insertResultCompletionStage.toCompletableFuture().get();

        List<InsertResult.MetricResult> items = new ArrayList<>();
        assertEquals(new InsertResult(true, false, items), insertResult);
    }

    @Test
    public void parsingElasticSearchClientErrorParseException() throws Exception {
        ElasticSearchDatasource elasticSearchDatasource = givenElasticSearchDatasourceThatReturnErrorParseException();
        Report report = givenAnEmptyReport();
        Application application = givenAnyApplication();

        CompletionStage<InsertResult> insertResultCompletionStage = elasticSearchDatasource.writeDataPoints(report, application);
        InsertResult insertResult = insertResultCompletionStage.toCompletableFuture().get();

        List<InsertResult.MetricResult> items = new ArrayList<>();
        assertEquals(new InsertResult(true, false, items), insertResult);
    }

    @Test
    public void parsingElasticSearchClientFailuresResponse() throws Exception {
        ElasticSearchDatasource elasticSearchDatasource = givenElasticSearchDatasourceThatReturnFailure();
        Report report = givenAReportWithOneDataPoint();
        Application application = givenAnyApplication();

        CompletionStage<InsertResult> insertResultCompletionStage = elasticSearchDatasource.writeDataPoints(report, application);
        InsertResult insertResult = insertResultCompletionStage.toCompletableFuture().get();

        List<InsertResult.MetricResult> items = new ArrayList<>();
        items.add(new InsertResult.MetricResult("", 0));
        assertEquals(new InsertResult(false, true, items), insertResult);
    }

    @Test
    public void doesNotDeleteAnyIndexIfTheIndexAreNotEmpty() throws Exception {
        ElasticSearchDatasource datasource = givenAElasticSearchDatasource();
        givenThereAreNoEmptyIndexes();

        datasource.deleteEmptyIndexes();

        verify(elasticsearchClient, never()).deleteIndex(anyString());
    }

    @Test
    public void deletesEmptyIndexes() throws Exception {
        ElasticSearchDatasource datasource = givenAElasticSearchDatasource();
        List<Index> indexes = givenThereAreSomeEmptyIndexes();

        datasource.deleteEmptyIndexes();

        for (Index index : indexes) {
            verify(elasticsearchClient).deleteIndex(index.getName());
        }
    }

    private List<Index> givenThereAreSomeEmptyIndexes() {
        return givenSomeIndexes(ANY_NUMBER_OF_INDEXES, 0);
    }

    @NotNull
    private List<Index> givenThereAreNoEmptyIndexes() {
        return givenSomeIndexes(ANY_NUMBER_OF_INDEXES, ANY_NUMBER_OF_DOCUMENTS);
    }

    private List<Index> givenSomeIndexes(int anyNumberOfIndexes, int anyNumberOfDocuments) {
        List<Index> indexes = new LinkedList<>();
        for (int i = 0; i < anyNumberOfIndexes; i++) {
            indexes.add(new Index("Index-" + i, anyNumberOfDocuments));
        }
        when(elasticsearchClient.getIndexes()).thenReturn(CompletableFuture.completedFuture(indexes));
        return indexes;
    }

    @NotNull
    private ElasticSearchDatasource givenElasticSearchDatasourceThatReturnTwoItems() {
        ActionWriteResponse networkDataResponse = new IndexResponse("flowup-network_data", "counter", "AVe4CB89xL5tw_jvDTTd", 1, true);
        networkDataResponse.setShardInfo(new ActionWriteResponse.ShardInfo(2, 1));
        ActionWriteResponse uiDataResponse = new IndexResponse("flowup-ui_data", "counter", "AVe4CB8-xL5tw_jvDTey", 1, true);
        uiDataResponse.setShardInfo(new ActionWriteResponse.ShardInfo(2, 1));
        BulkItemResponse[] responses = {new BulkItemResponse(0, "index", networkDataResponse), new BulkItemResponse(0, "index", uiDataResponse)};
        BulkResponse bulkResponse = new BulkResponse(responses, 67);

        ElasticSearchDatasource elasticSearchDatasource = givenAElasticSearchDatasource();
        when(elasticsearchClient.postBulk(anyListOf(IndexRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(bulkResponse));
        return elasticSearchDatasource;
    }

    private ElasticSearchDatasource givenAElasticSearchDatasource() {
        return app.injector().instanceOf(ElasticSearchDatasource.class);
    }

    @NotNull
    private ElasticSearchDatasource givenElasticSearchDatasourceThatReturnAggregation() {
        return loadElasticSearchDatasourceMSearchFromFile("elasticsearch/es_msearch_response.json");
    }


    @NotNull
    private ElasticSearchDatasource givenElasticSearchDatasourceThatReturnOneItem() {
        return loadElasticSearchDatasourcePostBulkFromFile("elasticsearch/es_simple_bulk_response.json");
    }


    @NotNull
    private ElasticSearchDatasource givenElasticSearchDatasourceThatReturnError() {
        return loadElasticSearchDatasourcePostBulkFromFile("elasticsearch/es_bulk_error.json");
    }

    @NotNull
    private ElasticSearchDatasource givenElasticSearchDatasourceThatReturnErrorParseException() {
        return loadElasticSearchDatasourcePostBulkFromFile("elasticsearch/es_bulk_error_parse_exception.json");
    }

    @NotNull
    private ElasticSearchDatasource givenElasticSearchDatasourceThatReturnFailure() {
        String fileName = "elasticsearch/es_bulk_failures.json";
        return loadElasticSearchDatasourcePostBulkFromFile(fileName);
    }

    @NotNull
    private ElasticSearchDatasource loadElasticSearchDatasourcePostBulkFromFile(String fileName) {
        JsonNode postBulkResult = Json.parse(getFile(fileName));
        BulkResponse bulkResponse = Json.fromJson(postBulkResult, BulkResponse.class);

        ElasticSearchDatasource elasticSearchDatasource = givenAElasticSearchDatasource();
        when(elasticsearchClient.postBulk(anyListOf(IndexRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(bulkResponse));
        return elasticSearchDatasource;
    }

    @NotNull
    private ElasticSearchDatasource loadElasticSearchDatasourceMSearchFromFile(String fileName) {
        JsonNode postMSearchResult = Json.parse(getFile(fileName));
        MSearchResponse mSearchResponse = Json.fromJson(postMSearchResult, MSearchResponse.class);

        ElasticSearchDatasource elasticSearchDatasource = givenAElasticSearchDatasource();
        when(elasticsearchClient.multiSearch(any()))
                .thenReturn(CompletableFuture.completedFuture(mSearchResponse));
        return elasticSearchDatasource;
    }
}
