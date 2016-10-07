import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Json;
import play.libs.ws.WSResponse;
import play.mvc.Http.RequestBuilder;
import play.mvc.Result;
import play.test.WithApplication;
import usecases.MetricsDatasource;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static play.inject.Bindings.bind;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;

/**
 *
 * Simple (JUnit) tests that can call all parts of a play app.
 * If you are interested in mocking a whole application, see the wiki for more details.
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ReportControllerTest extends WithApplication {

    @Mock
    private MetricsDatasource metricsDatasource;

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder()
                .overrides(bind(MetricsDatasource.class).toInstance(metricsDatasource))
                .build();
    }

    @Test
    public void testReportAPI() {
        RequestBuilder requestBuilder = fakeRequest("POST", "/report")
            .bodyText(getFile("reportRequest.json"))
            .header("Content-Type", "application/json");
        when(metricsDatasource.writeFakeCounter()).thenReturn(CompletableFuture.completedFuture(mock(WSResponse.class)));

        Result result = route(requestBuilder);

        assertEqualsString("{\"message\":\"Metrics Inserted\"}", result);
    }


    @Test
    public void testEmptyReport() {
        RequestBuilder requestBuilder = fakeRequest("POST", "/report")
                .bodyText(getFile("EmptyReportRequest.json"))
                .header("Content-Type", "application/json");
        when(metricsDatasource.writeFakeCounter()).thenReturn(CompletableFuture.completedFuture(mock(WSResponse.class)));

        Result result = route(requestBuilder);

        assertEqualsString("{\"message\":\"Metrics Inserted\"}", result);
    }

    @Test
    public void testMalformedReportReport() {
        RequestBuilder requestBuilder = fakeRequest("POST", "/report")
                .bodyText(getFile("MalformedReportRequest.json"))
                .header("Content-Type", "application/json");
        when(metricsDatasource.writeFakeCounter()).thenReturn(CompletableFuture.completedFuture(mock(WSResponse.class)));

        Result result = route(requestBuilder);

        assertEquals(BAD_REQUEST, result.status());
        assertTrue(contentAsJson(result).has("message"));
        assertTrue(contentAsJson(result).get("message").asText().contains("Error decoding json body"));
    }


    private void assertEqualsString(String expect, Result result) {
        assertEquals(expect, contentAsString(result));
        assertEquals(OK, result.status());
        assertEquals("application/json", result.contentType().get());
        assertEquals("UTF-8", result.charset().get());
    }

    private String getFile(String fileName){

        String result = "";

        ClassLoader classLoader = getClass().getClassLoader();
        try {
            result = IOUtils.toString(classLoader.getResourceAsStream(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;

    }

    public static JsonNode contentAsJson(Result result) {
        return Json.parse(contentAsString(result));
    }
}
