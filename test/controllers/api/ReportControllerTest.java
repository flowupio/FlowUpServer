package controllers.api;

import datasources.database.OrganizationDatasource;
import datasources.elasticsearch.*;
import models.ApiKey;
import models.Platform;
import models.Version;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.runners.MockitoJUnitRunner;
import play.Application;
import play.mvc.Http;
import play.mvc.Result;
import usecases.DashboardsClient;
import usecases.repositories.ApiKeyRepository;
import utils.WithDashboardsClient;
import utils.WithFlowUpApplication;
import utils.WithResources;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.*;
import static play.inject.Bindings.bind;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.CREATED;
import static play.test.Helpers.*;

@RunWith(MockitoJUnitRunner.class)
public class ReportControllerTest extends WithFlowUpApplication implements WithResources, WithDashboardsClient {

    private static final String API_KEY_VALUE = "35e25a2d1eaa464bab565f7f5e4bb029";
    private static final Version ANY_DEBUG_VERSION = new Version(0, 2, 8, Platform.ANDROID, true);
    private static final Version VERSION_1 = new Version(0, 2, 8, Platform.ANDROID);
    private static final Version VERSION_1_DEBUG = new Version(0, 2, 8, Platform.ANDROID, true);
    private static final Version VERSION_2 = new Version(0, 3, 0, Platform.ANDROID);
    private static final String ANY_UUID = "12345";

    @Captor
    private ArgumentCaptor<List<IndexRequest>> argument;
    private ApiKeyRepository apiKeyRepository;

    @Override
    protected Application provideApplication() {
        return getGuiceApplicationBuilder()
                .overrides(bind(DashboardsClient.class).toInstance(getMockDashboardsClient()))
                .build();
    }

    @Before
    @Override
    public void startPlay() {
        super.startPlay();
        apiKeyRepository = app.injector().instanceOf(ApiKeyRepository.class);
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

        assertRegularReportIsSaved(result);
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

        assertEquals(CREATED, result.status());
        String expect = "{\"message\":\"Metrics Inserted\",\"result\":{\"hasFailures\":false,\"items\":[],\"error\":false}}";
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
    public void returnsPreconditionFailedResponseIfTheSamplingGroupIsFull() {
        ApiKey apiKey = setupDatabaseWithApiKey(false);
        configureAFullSamplingGroup(apiKey);
        setupSuccessfulElasticsearchClient();

        Http.RequestBuilder requestBuilder = fakeRequest("POST", "/report")
                .bodyText(getFile("reportRequest.json"))
                .header("X-Api-Key", API_KEY_VALUE)
                .header("Content-Type", "application/json");

        Result result = route(requestBuilder);

        assertEquals(PRECONDITION_FAILED, result.status());
    }

    @Test
    public void savesReportsIfTheVersionIsDebuggableEvenIfTheSamplingGroupIsFull() {
        ApiKey apiKey = setupDatabaseWithApiKey(true);
        configureAFullSamplingGroup(apiKey);
        setupSuccessfulElasticsearchClient();

        Http.RequestBuilder requestBuilder = fakeRequest("POST", "/report")
                .bodyText(getFile("reportRequest.json"))
                .header("X-Api-Key", API_KEY_VALUE)
                .header("Content-Type", "application/json")
                .header("User-Agent", ANY_DEBUG_VERSION.toString());

        Result result = route(requestBuilder);

        assertEquals(CREATED, result.status());
    }

    @Test
    public void savesReportsIfTheVersionIsDebuggableEvenIfTheSamplingGroupIsNotFull() {
        setupDatabaseWithApiKey(true);
        setupSuccessfulElasticsearchClient();

        Http.RequestBuilder requestBuilder = fakeRequest("POST", "/report")
                .bodyText(getFile("reportRequest.json"))
                .header("X-Api-Key", API_KEY_VALUE)
                .header("Content-Type", "application/json")
                .header("User-Agent", ANY_DEBUG_VERSION.toString());

        Result result = route(requestBuilder);

        assertEquals(CREATED, result.status());
    }

    @Test
    public void testReportAPIInBackgroundAndDebugModeWontInsertAnyMetric() {
        setupDatabaseWithApiKey();
        setupSuccessfulElasticsearchClient();
        Http.RequestBuilder requestBuilder = fakeRequest("POST", "/report")
                .bodyText(getFile("androidsdk/multipleCPUMetricReportRequestBody.json"))
                .header("X-Api-Key", API_KEY_VALUE)
                .header("X-Debug-Mode", "true")
                .header("Content-Type", "application/json");

        Result result = route(requestBuilder);

        verify(elasticsearchClient, never()).postBulk(argument.capture());
        assertEquals(CREATED, result.status());
        String expect = "{\"message\":\"Metrics Inserted\",\"result\":{\"hasFailures\":false,\"items\":[],\"error\":false}}";
        assertEqualsString(expect, result);
    }

    @Test
    public void acceptsReportsIfTheAndroidSDKVersionUsedAsUserAgentHeaderContainsTheSameVersion() {
        setupDatabaseWithApiKey(true, VERSION_1);
        setupSuccessfulElasticsearchClient();
        Http.RequestBuilder requestBuilder = fakeRequest("POST", "/report")
                .bodyText(getFile("reportRequest.json"))
                .header("X-Api-Key", API_KEY_VALUE)
                .header("Content-Type", "application/json")
                .header("X-UUID", ANY_UUID)
                .header("User-Agent", VERSION_1.toString());

        Result result = route(requestBuilder);

        assertRegularReportIsSaved(result);
    }

    @Test
    public void acceptsReportsIfTheAndroidSDKVersionUsedAsUserAgentIsSupported() {
        setupDatabaseWithApiKey(true, VERSION_1);
        setupSuccessfulElasticsearchClient();
        Http.RequestBuilder requestBuilder = fakeRequest("POST", "/report")
                .bodyText(getFile("reportRequest.json"))
                .header("X-Api-Key", API_KEY_VALUE)
                .header("Content-Type", "application/json")
                .header("X-UUID", ANY_UUID)
                .header("User-Agent", VERSION_2.toString());

        Result result = route(requestBuilder);

        assertRegularReportIsSaved(result);
    }

    @Test
    public void doesNotAcceptReportsIfTheAndroidSDKVersionUsedAsUserAgentIsNotSupported() {
        setupDatabaseWithApiKey(true, VERSION_2);
        setupSuccessfulElasticsearchClient();
        Http.RequestBuilder requestBuilder = fakeRequest("POST", "/report")
                .bodyText(getFile("reportRequest.json"))
                .header("X-Api-Key", API_KEY_VALUE)
                .header("Content-Type", "application/json")
                .header("X-UUID", ANY_UUID)
                .header("User-Agent", VERSION_1.toString());

        Result result = route(requestBuilder);

        assertEquals(FORBIDDEN, result.status());
    }

    @Test
    public void doesNotAcceptReportsIfTheAndroidSDKVersionUsedAsUserAgentIsNotSupportedEvenIfIsDebuggable() {
        setupDatabaseWithApiKey(true, VERSION_2);
        setupSuccessfulElasticsearchClient();
        Http.RequestBuilder requestBuilder = fakeRequest("POST", "/report")
                .bodyText(getFile("reportRequest.json"))
                .header("X-Api-Key", API_KEY_VALUE)
                .header("Content-Type", "application/json")
                .header("X-UUID", ANY_UUID)
                .header("User-Agent", VERSION_1_DEBUG.toString());

        Result result = route(requestBuilder);

        assertEquals(FORBIDDEN, result.status());
    }

    @Test
    public void doesNotAcceptReportsWithoutTheUserAgentHeader() {
        setupDatabaseWithApiKey(true, VERSION_1);
        setupSuccessfulElasticsearchClient();
        Http.RequestBuilder requestBuilder = fakeRequest("POST", "/report")
                .bodyText(getFile("reportRequest.json"))
                .header("X-Api-Key", API_KEY_VALUE)
                .header("Content-Type", "application/json")
                .header("X-UUID", ANY_UUID);

        Result result = route(requestBuilder);

        assertEquals(FORBIDDEN, result.status());
    }

    private void configureAFullSamplingGroup(ApiKey apiKey) {
        for (int i = 0; i < 50; i++) {
            apiKeyRepository.addAllowedUUID(apiKey, UUID.randomUUID().toString());
        }
    }

    private void setupDatabaseWithApiKey() {
        setupDatabaseWithApiKey(true);
    }

    private ApiKey setupDatabaseWithApiKey(boolean enabled) {
        return setupDatabaseWithApiKey(enabled, null);
    }

    private ApiKey setupDatabaseWithApiKey(boolean enabled, Version minAndroidSdkVersion) {
        ApiKey apiKey = apiKeyRepository.create(API_KEY_VALUE, enabled, minAndroidSdkVersion);
        new OrganizationDatasource(apiKeyRepository).create("example", "@example.com", apiKey, "");
        return apiKey;
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
        ActionWriteResponse networkDataResponse = new IndexResponse("flowup-network_data", "counter", "AVe4CB89xL5tw_jvDTTd", 1, null, 429);
        BulkError error = new BulkError();
        error.setType("es_rejected_execution_exception");
        error.setReason("rejected execution of org.elasticsearch.transport.TransportService$4@525ba0d5 on EsThreadPoolExecutor[bulk, queue capacity = 50, org.elasticsearch.common.util.concurrent.EsThreadPoolExecutor@65d320c9[Running, pool size = 1, active threads = 1, queued tasks = 50, completed tasks = 18719]]");
        networkDataResponse.setError(error);
        BulkItemResponse[] responses = {new BulkItemResponse(0, "index", networkDataResponse)};
        BulkResponse bulkResponse = new BulkResponse(responses, 67);

        when(elasticsearchClient.postBulk(anyListOf(IndexRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(bulkResponse));
    }

    private void assertRegularReportIsSaved(Result result) {
        verify(elasticsearchClient).postBulk(argument.capture());
        assertEquals(5, argument.getValue().size());
        assertEquals(CREATED, result.status());
        String expect = "{\"message\":\"Metrics Inserted\",\"result\":{\"hasFailures\":false,\"items\":[{\"name\":\"\",\"successful\":1}],\"error\":false}}";
        assertEqualsString(expect, result);
    }

}
