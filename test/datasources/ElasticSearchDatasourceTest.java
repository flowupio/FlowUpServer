package datasources;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import usecases.InsertResult;
import utils.WithResources;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

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

        CompletionStage<InsertResult> insertResultCompletionStage = elasticSearchDatasource.writeDataPoints(new ArrayList<>());
        InsertResult insertResult = insertResultCompletionStage.toCompletableFuture().get();

        List<InsertResult.MetricResult> items = new ArrayList<>();
        items.add(new InsertResult.MetricResult("network_data", 1));
        items.add(new InsertResult.MetricResult("ui_data", 1));
        assertEquals(new InsertResult(false, items), insertResult);
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

}
