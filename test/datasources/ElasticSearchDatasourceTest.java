package datasources;

import com.fasterxml.jackson.databind.JsonNode;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.libs.Json;
import usecases.InsertResult;
import usecases.Report;
import utils.WithResources;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ElasticSearchDatasourceTest implements WithResources {

    @Mock
    private ElasticsearchClient elasticsearchClient;

    @Test
    public void parsingElasticSearchClientResponse() throws Exception {
        ElasticSearchDatasource elasticSearchDatasource = givenElasticSearchDatasourceThatReturnTwoItems();
        Report report = givenAnEmptyReport();

        CompletionStage<InsertResult> insertResultCompletionStage = elasticSearchDatasource.writeDataPoints(report);
        InsertResult insertResult = insertResultCompletionStage.toCompletableFuture().get();

        List<InsertResult.MetricResult> items = new ArrayList<>();
        items.add(new InsertResult.MetricResult("network_data", 1));
        items.add(new InsertResult.MetricResult("ui_data", 1));
        assertEquals(new InsertResult(false, false, items), insertResult);
    }

    @NotNull
    private Report givenAnEmptyReport() {
        String organizationIdentifier = "3e02e6b9-3a33-4113-ae78-7d37f11ca3bf";
        return new Report(organizationIdentifier, "io.flowup.app", new ArrayList<>());
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
        Report report = givenAnEmptyReport();

        CompletionStage<InsertResult> insertResultCompletionStage = elasticSearchDatasource.writeDataPoints(report);
        InsertResult insertResult = insertResultCompletionStage.toCompletableFuture().get();

        List<InsertResult.MetricResult> items = new ArrayList<>();
        items.add(new InsertResult.MetricResult("network_data", 1));
        assertEquals(new InsertResult(false, false, items), insertResult);
    }

    @Test
    public void parsingElasticSearchClientErrorResponse() throws Exception {
        ElasticSearchDatasource elasticSearchDatasource = givenElasticSearchDatasourceThatReturnError();
        Report report = givenAnEmptyReport();

        CompletionStage<InsertResult> insertResultCompletionStage = elasticSearchDatasource.writeDataPoints(report);
        InsertResult insertResult = insertResultCompletionStage.toCompletableFuture().get();

        List<InsertResult.MetricResult> items = new ArrayList<>();
        assertEquals(new InsertResult(true, false, items), insertResult);
    }

    @Test
    public void parsingElasticSearchClientFailuresResponse() throws Exception {
        ElasticSearchDatasource elasticSearchDatasource = givenElasticSearchDatasourceThatReturnFailure();
        Report report = givenAnEmptyReport();

        CompletionStage<InsertResult> insertResultCompletionStage = elasticSearchDatasource.writeDataPoints(report);
        InsertResult insertResult = insertResultCompletionStage.toCompletableFuture().get();

        List<InsertResult.MetricResult> items = new ArrayList<>();
        items.add(new InsertResult.MetricResult("network_data", 0));
        assertEquals(new InsertResult(false, true, items), insertResult);
    }

    @NotNull
    private ElasticSearchDatasource givenElasticSearchDatasourceThatReturnTwoItems() {
        ActionWriteResponse networkDataResponse = new IndexResponse("statsd-network_data", "counter", "AVe4CB89xL5tw_jvDTTd", 1, true);
        networkDataResponse.setShardInfo(new ActionWriteResponse.ShardInfo(2, 1));
        ActionWriteResponse uiDataResponse = new IndexResponse("statsd-ui_data", "counter", "AVe4CB8-xL5tw_jvDTey", 1, true);
        uiDataResponse.setShardInfo(new ActionWriteResponse.ShardInfo(2, 1));
        BulkItemResponse[] responses = {new BulkItemResponse(0, "index", networkDataResponse), new BulkItemResponse(0, "index", uiDataResponse)};
        BulkResponse bulkResponse = new BulkResponse(responses, 67);

        ElasticSearchDatasource elasticSearchDatasource = new ElasticSearchDatasource(elasticsearchClient);
        when(elasticsearchClient.postBulk(anyListOf(IndexRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(bulkResponse));
        return elasticSearchDatasource;
    }

    @NotNull
    private ElasticSearchDatasource givenElasticSearchDatasourceThatReturnOneItem() {
        return loadElasticSearchDatasourceFromFile("elasticsearch/es_simple_bulk_response.json");
    }


    @NotNull
    private ElasticSearchDatasource givenElasticSearchDatasourceThatReturnError() {
        return loadElasticSearchDatasourceFromFile("elasticsearch/es_bulk_error.json");
    }

    @NotNull
    private ElasticSearchDatasource givenElasticSearchDatasourceThatReturnFailure() {
        String fileName = "elasticsearch/es_bulk_failures.json";
        return loadElasticSearchDatasourceFromFile(fileName);
    }

    @NotNull
    private ElasticSearchDatasource loadElasticSearchDatasourceFromFile(String fileName) {
        JsonNode postBulkResult = Json.parse(getFile(fileName));
        BulkResponse bulkResponse = Json.fromJson(postBulkResult, BulkResponse.class);

        ElasticSearchDatasource elasticSearchDatasource = new ElasticSearchDatasource(elasticsearchClient);
        when(elasticsearchClient.postBulk(anyListOf(IndexRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(bulkResponse));
        return elasticSearchDatasource;
    }
}
