import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
import play.mvc.Http.RequestBuilder;
import play.mvc.Result;
import play.test.WithApplication;
import utils.WithResources;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static play.inject.Bindings.bind;
import static play.test.Helpers.*;

@RunWith(MockitoJUnitRunner.class)
public class ReportControllerTest extends WithApplication implements WithResources {

    @Mock
    private ElasticsearchClient elasticsearchClient;

    @Captor
    private ArgumentCaptor<List<JsonNode>> argument;

    @Override
    protected Application provideApplication() {
        ArrayNode items = Json.newArray();
        items.add(Json.newObject().set("create", Json.newObject().put("_index", "statsd-network_data").put("successful", 1)));
        JsonNode postBulkResult = Json.newObject()
                .put("errors", false)
                .set("items", items);

        when(elasticsearchClient.postBulk(anyListOf(JsonNode.class))).thenReturn(CompletableFuture.completedFuture(postBulkResult));

        return new GuiceApplicationBuilder()
                .overrides(bind(ElasticsearchClient.class).toInstance(elasticsearchClient))
                .build();
    }

    @Test
    public void testReportAPI() {
        RequestBuilder requestBuilder = fakeRequest("POST", "/report")
            .bodyText(getFile("reportRequest.json"))
            .header("Content-Type", "application/json");

        Result result = route(requestBuilder);

        verify(elasticsearchClient).postBulk(argument.capture());
        assertEquals(4, argument.getValue().size());
        assertEquals(CREATED, result.status());
        assertEqualsString("{\"message\":\"Metrics Inserted\",\"result\":{\"hasErrors\":false,\"items\":[{\"name\":\"network_data\",\"successful\":1}]}}", result);
    }


    @Test
    public void testEmptyReport() {
        RequestBuilder requestBuilder = fakeRequest("POST", "/report")
                .bodyText(getFile("EmptyReportRequest.json"))
                .header("Content-Type", "application/json");

        Result result = route(requestBuilder);

        verify(elasticsearchClient).postBulk(argument.capture());
        assertEquals(0, argument.getValue().size());
        assertEquals(CREATED, result.status());
        assertEqualsString("{\"message\":\"Metrics Inserted\",\"result\":{\"hasErrors\":false,\"items\":[{\"name\":\"network_data\",\"successful\":1}]}}", result);
    }

    @Test
    public void testWrongAPIFormat() {
        RequestBuilder requestBuilder = fakeRequest("POST", "/report")
                .bodyText(getFile("WrongAPIFormat.json"))
                .header("Content-Type", "application/json");

        Result result = route(requestBuilder);

        assertEquals(BAD_REQUEST, result.status());
        assertThat(contentAsString(result), containsString("Unable to read class"));
    }

//    @Test
//    public void testMalformedReportReport() {
//        RequestBuilder requestBuilder = fakeRequest("POST", "/report")
//                .bodyText(getFile("MalformedReportRequest.json"))
//                .header("Content-Type", "application/json");
//        when(metricsDatasource.writeDataPoints()).thenReturn(CompletableFuture.completedFuture(mock(WSResponse.class)));
//
//        Result result = route(requestBuilder);
//
//        assertEquals(BAD_REQUEST, result.status());
//        assertTrue(contentAsJson(result).has("message"));
//        assertTrue(contentAsJson(result).get("message").asText().contains("Error decoding json body"));
//    }
}
