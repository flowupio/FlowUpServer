package controllers;


import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
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
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;
import utils.WithResources;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static play.inject.Bindings.bind;
import static play.mvc.Http.Status.BAD_REQUEST;
import static play.mvc.Http.Status.CREATED;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.route;

@RunWith(MockitoJUnitRunner.class)
public class ReportControllerTest extends WithApplication implements WithResources {

    @Mock
    private ElasticsearchClient elasticsearchClient;

    @Captor
    private ArgumentCaptor<List<JsonNode>> argument;

    @Override
    protected Application provideApplication() {
        JsonNode postBulkResult = Json.parse(getFile("elasticsearch/es_simple_bulk_response.json"));
        when(elasticsearchClient.postBulk(anyListOf(JsonNode.class))).thenReturn(CompletableFuture.completedFuture(postBulkResult));

        return new GuiceApplicationBuilder()
                .configure((Map) Helpers.inMemoryDatabase("flowupdb", ImmutableMap.of("MODE", "MYSQL")))
                .overrides(bind(ElasticsearchClient.class).toInstance(elasticsearchClient))
                .build();
    }

    @Test
    public void testReportAPI() {
        Http.RequestBuilder requestBuilder = fakeRequest("POST", "/report")
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
        Http.RequestBuilder requestBuilder = fakeRequest("POST", "/report")
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
        Http.RequestBuilder requestBuilder = fakeRequest("POST", "/report")
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
