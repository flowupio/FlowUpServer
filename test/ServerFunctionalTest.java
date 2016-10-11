import org.junit.Test;
import play.libs.ws.WS;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.test.WithServer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletionStage;
import java.util.zip.GZIPOutputStream;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.CREATED;
import static play.mvc.Http.Status.REQUEST_ENTITY_TOO_LARGE;

public class ServerFunctionalTest extends WithServer implements WithResources {

    @Test
    public void testRequestTooLarge() throws Exception {
        String url = "http://localhost:" + this.testServer.port() + "/report";
        try (WSClient ws = WS.newClient(this.testServer.port())) {
            CompletionStage<WSResponse> stage = ws.url(url)
                    .setContentType("application/json")
                    .setHeader("Content-Encoding", "gzip")
                    .post(new ByteArrayInputStream(gzip(getFile("aBunchOfData.json"))));
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
                    .post(new ByteArrayInputStream(gzip(getFile("9andAhalfMB.json"))));
            WSResponse response = stage.toCompletableFuture().get();
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
