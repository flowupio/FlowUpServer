package datasources;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.libs.Json;
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
        JsonNode postBulkResult = Json.parse(getFile("es_bulk_response.json"));
        ElasticSearchDatasource elasticSearchDatasource = new ElasticSearchDatasource(elasticsearchClient);
        when(elasticsearchClient.postBulk(anyListOf(JsonNode.class)))
                .thenReturn(CompletableFuture.completedFuture(postBulkResult));

        CompletionStage<InsertResult> insertResultCompletionStage = elasticSearchDatasource.writeDataPoints(new ArrayList<>());
        InsertResult insertResult = insertResultCompletionStage.toCompletableFuture().get();

        List<InsertResult.MetricResult> items = new ArrayList<>();
        items.add(new InsertResult.MetricResult("network_data", 1));
        items.add(new InsertResult.MetricResult("ui_data", 1));
        assertEquals(new InsertResult(false, items), insertResult);
    }

}