import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import datasources.ElasticsearchClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.test.Helpers;
import play.test.WithServer;
import utils.WithResources;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.zip.GZIPOutputStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static play.inject.Bindings.bind;
import static play.mvc.Http.Status.CREATED;
import static play.mvc.Http.Status.REQUEST_ENTITY_TOO_LARGE;

@RunWith(MockitoJUnitRunner.class)
public class ServerFunctionalTest extends WithServer implements WithResources {

    @Mock
    private ElasticsearchClient elasticsearchClient;

    @Captor
    private ArgumentCaptor<List<JsonNode>> argument;

    @Override
    protected Application provideApplication() {
        ObjectNode postBulkResult = Json.newObject()
                .put("errors", false)
                .putPOJO("items", new ArrayList());
        when(elasticsearchClient.postBulk(anyList())).thenReturn(CompletableFuture.completedFuture(postBulkResult));

        return new GuiceApplicationBuilder()
                .configure((Map) Helpers.inMemoryDatabase())
                .overrides(bind(ElasticsearchClient.class).toInstance(elasticsearchClient))
                .build();
    }

    @Test
    public void testRequestTooLarge() throws Exception {
        String url = "http://localhost:" + this.testServer.port() + "/report";
        try (WSClient ws = WS.newClient(this.testServer.port())) {
            CompletionStage<WSResponse> stage = ws.url(url)
                    .setContentType("application/json")
                    .setHeader("Content-Encoding", "gzip")
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
                    .post(new ByteArrayInputStream(gzip(getFile("9andAhalfMBRequest.json"))));
            WSResponse response = stage.toCompletableFuture().get();

            verify(elasticsearchClient).postBulk(argument.capture());
            assertEquals(15664, argument.getValue().size());
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
