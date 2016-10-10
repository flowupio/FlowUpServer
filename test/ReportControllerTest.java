import com.fasterxml.jackson.databind.JsonNode;
import datasources.ElasticsearchClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http.RequestBuilder;
import play.mvc.Result;
import play.test.WithApplication;

import java.util.concurrent.CompletableFuture;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static play.inject.Bindings.bind;
import static play.test.Helpers.*;

@RunWith(MockitoJUnitRunner.class)
public class ReportControllerTest extends WithApplication implements WithResources {

    @Mock
    private ElasticsearchClient elasticsearchClient;

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder()
                .overrides(bind(ElasticsearchClient.class).toInstance(elasticsearchClient))
                .build();
    }

    @Test
    public void testReportAPI() {
        RequestBuilder requestBuilder = fakeRequest("POST", "/report")
            .bodyText(getFile("reportRequest.json"))
            .header("Content-Type", "application/json");
        when(elasticsearchClient.postBulk(anyString())).thenReturn(CompletableFuture.completedFuture(mock(JsonNode.class)));

        Result result = route(requestBuilder);

        assertEquals(CREATED, result.status());
        assertEqualsString("{\"message\":\"Metrics Inserted\"}", result);
    }


    @Test
    public void testEmptyReport() {
        RequestBuilder requestBuilder = fakeRequest("POST", "/report")
                .bodyText(getFile("EmptyReportRequest.json"))
                .header("Content-Type", "application/json");
        when(elasticsearchClient.postBulk(anyString())).thenReturn(CompletableFuture.completedFuture(mock(JsonNode.class)));

        Result result = route(requestBuilder);

        assertEquals(CREATED, result.status());
        assertEqualsString("{\"message\":\"Metrics Inserted\"}", result);
    }

    @Test
    public void testWrongAPIFormat() {
        RequestBuilder requestBuilder = fakeRequest("POST", "/report")
                .bodyText(getFile("WrongAPIFormat.json"))
                .header("Content-Type", "application/json");
        when(elasticsearchClient.postBulk(anyString())).thenReturn(CompletableFuture.completedFuture(mock(JsonNode.class)));

        Result result = route(requestBuilder);

        assertEquals(BAD_REQUEST, result.status());
        assertThat(contentAsString(result), containsString("Unable to read class"));
    }

//    @Test
//    public void testMalformedReportReport() {
//        RequestBuilder requestBuilder = fakeRequest("POST", "/report")
//                .bodyText(getFile("MalformedReportRequest.json"))
//                .header("Content-Type", "application/json");
//        when(metricsDatasource.writeFakeCounter()).thenReturn(CompletableFuture.completedFuture(mock(WSResponse.class)));
//
//        Result result = route(requestBuilder);
//
//        assertEquals(BAD_REQUEST, result.status());
//        assertTrue(contentAsJson(result).has("message"));
//        assertTrue(contentAsJson(result).get("message").asText().contains("Error decoding json body"));
//    }
}
