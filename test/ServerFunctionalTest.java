
import datasources.database.ApiKeyDatasource;
import datasources.database.OrganizationDatasource;
import datasources.elasticsearch.BulkItemResponse;
import datasources.elasticsearch.BulkResponse;
import datasources.elasticsearch.ElasticsearchClient;
import datasources.elasticsearch.IndexRequest;
import usecases.DashboardsClient;
import models.ApiKey;
import models.Organization;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.Application;
import play.cache.CacheApi;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.ws.WS;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.test.WithServer;
import usecases.repositories.ApiKeyRepository;
import utils.Time;
import utils.WithDashboardsClient;
import utils.WithResources;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.zip.GZIPOutputStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static play.inject.Bindings.bind;
import static play.mvc.Http.Status.CREATED;
import static play.mvc.Http.Status.REQUEST_ENTITY_TOO_LARGE;

@RunWith(MockitoJUnitRunner.class)
public class ServerFunctionalTest extends WithServer implements WithResources, WithDashboardsClient {

    @Mock
    private ElasticsearchClient elasticsearchClient;

    @Captor
    private ArgumentCaptor<List<IndexRequest>> argument;

    public static final String API_KEY_VALUE = "35e25a2d1eaa464bab565f7f5e4bb029";
    private ApiKey apiKey;
    private Organization organization;

    @After
    public void tearDown() {
        organization.delete();
        apiKey.delete();
    }

    @Override
    protected Application provideApplication() {
        setupElasticsearchClient();

        return new GuiceApplicationBuilder()
                .overrides(bind(DashboardsClient.class).toInstance(getMockDashboardsClient()))
                .overrides(bind(ElasticsearchClient.class).toInstance(elasticsearchClient))
                .build();
    }

    @Before
    public void setupDatabaseWithApiKey() {
        ApiKeyRepository apiKeyRepository = new ApiKeyRepository(app.injector().instanceOf(CacheApi.class),
                new ApiKeyDatasource(new Time()), new Time());
        this.apiKey = apiKeyRepository.create(API_KEY_VALUE);
        this.organization = new OrganizationDatasource(apiKeyRepository).create("example", "@example.com", apiKey);
    }

    private void setupElasticsearchClient() {
        BulkItemResponse[] responses = {};
        BulkResponse bulkResponse = new BulkResponse(responses, 67);

        when(elasticsearchClient.postBulk(anyListOf(IndexRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(bulkResponse));
    }

    @Test
    public void testRequestTooLarge() throws Exception {
        String url = "http://localhost:" + this.testServer.port() + "/report";
        try (WSClient ws = WS.newClient(this.testServer.port())) {
            CompletionStage<WSResponse> stage = ws.url(url)
                    .setContentType("application/json")
                    .setHeader("Content-Encoding", "gzip")
                    .setHeader("X-Api-Key", API_KEY_VALUE)
                    .post(new ByteArrayInputStream(gzip(getFile("TooLargeRequest.json"))));
            WSResponse response = stage.toCompletableFuture().get();

            assertEquals(REQUEST_ENTITY_TOO_LARGE, response.getStatus());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGzipRequest() throws Exception {
        String url = "http://localhost:" + this.testServer.port() + "/report";
        try (WSClient ws = WS.newClient(this.testServer.port())) {
            CompletionStage<WSResponse> stage = ws.url(url)
                    .setContentType("application/json")
                    .setHeader("Content-Encoding", "gzip")
                    .setHeader("X-Api-Key", API_KEY_VALUE)
                    .post(new ByteArrayInputStream(gzip(getFile("7832MetricsRequest.json"))));
            WSResponse response = stage.toCompletableFuture().get();

            verify(elasticsearchClient).postBulk(argument.capture());
            assertEquals(7832, argument.getValue().size());
            assertEquals(CREATED, response.getStatus());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private byte[] gzip(String str) throws IOException {
        if ((str == null) || (str.length() == 0)) {
            return null;
        }
        ByteArrayOutputStream obj = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(obj);
        gzip.write(str.getBytes("UTF-8"));
        gzip.close();
        return obj.toByteArray();
    }
}
