package controllers;

import datasources.database.OrganizationDatasource;
import datasources.elasticsearch.*;
import models.ApiKey;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;
import play.mvc.Result;
import usecases.repositories.ApiKeyRepository;
import utils.WithFlowUpApplication;
import utils.WithResources;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static play.inject.Bindings.bind;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.CREATED;
import static play.test.Helpers.*;

@RunWith(MockitoJUnitRunner.class)
public class ReportControllerTest extends WithFlowUpApplication implements WithResources {

    private static final String API_KEY_VALUE = "35e25a2d1eaa464bab565f7f5e4bb029";

    @Mock
    private ElasticsearchClient elasticsearchClient;

    @Captor
    private ArgumentCaptor<List<IndexRequest>> argument;
    private ApiKeyRepository apiKeyRepository;

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder()
                .overrides(bind(ElasticsearchClient.class).toInstance(elasticsearchClient))
                .build();
    }

    @Before
    @Override
    public void startPlay() {
        super.startPlay();
        apiKeyRepository = app.injector().instanceOf(ApiKeyRepository.class);
    }

    private void setupDatabaseWithApiKey() {
        setupDatabaseWithApiKey(true);
    }

    private void setupDatabaseWithApiKey(boolean enabled) {
        ApiKey apiKey = apiKeyRepository.create(API_KEY_VALUE, enabled);
        new OrganizationDatasource(apiKeyRepository).create("example", "@example.com", apiKey);
    }


    private void setupSuccessfulElasticsearchClient() {
        ActionWriteResponse networkDataResponse = new IndexResponse("statsd-network_data", "counter", "AVe4CB89xL5tw_jvDTTd", 1, true);
        networkDataResponse.setShardInfo(new ActionWriteResponse.ShardInfo(2, 1));
        BulkItemResponse[] responses = {new BulkItemResponse(0, "index", networkDataResponse)};
        BulkResponse bulkResponse = new BulkResponse(responses, 67);

        when(elasticsearchClient.postBulk(anyListOf(IndexRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(bulkResponse));
    }

    private void setupElasticsearchClientWithError() {
        BulkResponse bulkResponse = new BulkResponse();
        BulkError error = new BulkError();
        error.setType("illegal_argument_exception");
        error.setReason("Malformed action/metadata line [1], expected a simple value for field [index] but found [START_OBJECT]");
        bulkResponse.setError(error);

        when(elasticsearchClient.postBulk(anyListOf(IndexRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(bulkResponse));
    }

    private void setupElasticsearchClientWithFailures() {
        ActionWriteResponse networkDataResponse = new IndexResponse("statsd-network_data", "counter", "AVe4CB89xL5tw_jvDTTd", 1, null, 429);
        BulkError error = new BulkError();
        error.setType("es_rejected_execution_exception");
        error.setReason("rejected execution of org.elasticsearch.transport.TransportService$4@525ba0d5 on EsThreadPoolExecutor[bulk, queue capacity = 50, org.elasticsearch.common.util.concurrent.EsThreadPoolExecutor@65d320c9[Running, pool size = 1, active threads = 1, queued tasks = 50, completed tasks = 18719]]");
        networkDataResponse.setError(error);
        BulkItemResponse[] responses = {new BulkItemResponse(0, "index", networkDataResponse)};
        BulkResponse bulkResponse = new BulkResponse(responses, 67);

        when(elasticsearchClient.postBulk(anyListOf(IndexRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(bulkResponse));
    }

    @Test
    public void testReportAPI() {
        setupDatabaseWithApiKey();
        setupSuccessfulElasticsearchClient();
        Http.RequestBuilder requestBuilder = fakeRequest("POST", "/report")
                .bodyText(getFile("reportRequest.json"))
                .header("X-Api-Key", API_KEY_VALUE)
                .header("Content-Type", "application/json");

        Result result = route(requestBuilder);

        verify(elasticsearchClient).postBulk(argument.capture());
        assertEquals(5, argument.getValue().size());
        assertEquals(CREATED, result.status());
        String expect = "{\"message\":\"Metrics Inserted\",\"result\":{\"hasFailures\":false,\"items\":[{\"name\":\"network_data\",\"successful\":1}],\"error\":false}}";
        assertEqualsString(expect, result);
    }

    @Test
    public void testReportAPIWithElasticSearchError() {
        setupDatabaseWithApiKey();
        setupElasticsearchClientWithError();
        Http.RequestBuilder requestBuilder = fakeRequest("POST", "/report")
                .bodyText(getFile("reportRequest.json"))
                .header("X-Api-Key", API_KEY_VALUE)
                .header("Content-Type", "application/json");

        Result result = route(requestBuilder);

        verify(elasticsearchClient).postBulk(argument.capture());
        assertEquals(5, argument.getValue().size());
        assertEquals(INTERNAL_SERVER_ERROR, result.status());
    }

    @Test
    public void testReportAPIWithElasticSearchFailures() {
        setupDatabaseWithApiKey();
        setupElasticsearchClientWithFailures();
        Http.RequestBuilder requestBuilder = fakeRequest("POST", "/report")
                .bodyText(getFile("reportRequest.json"))
                .header("X-Api-Key", API_KEY_VALUE)
                .header("Content-Type", "application/json");

        Result result = route(requestBuilder);

        verify(elasticsearchClient).postBulk(argument.capture());
        assertEquals(5, argument.getValue().size());
        assertEquals(SERVICE_UNAVAILABLE, result.status());
    }

    @Test
    public void testEmptyReport() {
        setupDatabaseWithApiKey();
        setupSuccessfulElasticsearchClient();
        Http.RequestBuilder requestBuilder = fakeRequest("POST", "/report")
                .bodyText(getFile("EmptyReportRequest.json"))
                .header("X-Api-Key", API_KEY_VALUE)
                .header("Content-Type", "application/json");

        Result result = route(requestBuilder);

        verify(elasticsearchClient).postBulk(argument.capture());
        assertEquals(0, argument.getValue().size());
        assertEquals(CREATED, result.status());
        String expect = "{\"message\":\"Metrics Inserted\",\"result\":{\"hasFailures\":false,\"items\":[{\"name\":\"network_data\",\"successful\":1}],\"error\":false}}";
        assertEqualsString(expect, result);
    }

    @Test
    public void testWrongAPIFormat() {
        setupDatabaseWithApiKey();
        setupSuccessfulElasticsearchClient();
        Http.RequestBuilder requestBuilder = fakeRequest("POST", "/report")
                .bodyText(getFile("WrongAPIFormat.json"))
                .header("X-Api-Key", API_KEY_VALUE)
                .header("Content-Type", "application/json");

        Result result = route(requestBuilder);

        assertEquals(BAD_REQUEST, result.status());
        assertThat(contentAsString(result), containsString("Unable to read class"));
    }

    @Test
    public void returnsPreconditionFailedResponseIfTheApiKeyIsDisabled() {
        setupDatabaseWithApiKey(false);
        setupSuccessfulElasticsearchClient();

        Http.RequestBuilder requestBuilder = fakeRequest("POST", "/report")
                .bodyText(getFile("reportRequest.json"))
                .header("X-Api-Key", API_KEY_VALUE)
                .header("Content-Type", "application/json");

        Result result = route(requestBuilder);

        assertEquals(PRECONDITION_FAILED, result.status());
    }

    @Test
    @Ignore
    public void testMalformedReportReport() {
        setupSuccessfulElasticsearchClient();
        Http.RequestBuilder requestBuilder = fakeRequest("POST", "/report")
                .bodyText(getFile("MalformedReportRequest.json"))
                .header("Content-Type", "application/json");

        Result result = route(requestBuilder);

        assertEquals(BAD_REQUEST, result.status());
        assertTrue(contentAsJson(result).has("message"));
        assertTrue(contentAsJson(result).get("message").asText().contains("Error decoding json body"));
    }
}
