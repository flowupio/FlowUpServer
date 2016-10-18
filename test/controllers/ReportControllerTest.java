package controllers;


import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import datasources.ElasticsearchClient;
import models.ApiKey;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.Application;
import play.db.Database;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;
import utils.WithDatabase;
import utils.WithResources;

import java.util.List;
import java.util.Map;
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
public class ReportControllerTest extends WithApplication implements WithResources, WithDatabase {

    private static final String API_KEY_VALUE = "35e25a2d1eaa464bab565f7f5e4bb029";

    private Database database;
    @Override
    public Database getDatabase() {
        return database;
    }

    @Override
    public void setDatabase(Database database) {
        this.database = database;
    }

    @Mock
    private ElasticsearchClient elasticsearchClient;

    @Captor
    private ArgumentCaptor<List<JsonNode>> argument;

    @Override
    protected Application provideApplication() {
        JsonNode postBulkResult = Json.parse(getFile("elasticsearch/es_simple_bulk_response.json"));
        when(elasticsearchClient.postBulk(anyListOf(JsonNode.class))).thenReturn(CompletableFuture.completedFuture(postBulkResult));

        return new GuiceApplicationBuilder()
                .overrides(bind(ElasticsearchClient.class).toInstance(elasticsearchClient))
                .configure((Map) Helpers.inMemoryDatabase("default", ImmutableMap.of(
                        "MODE", "MYSQL"
                )))
                .overrides(bind(Database.class).toInstance(getDatabase()))
                .build();
    }

    @Test
    public void testReportAPI() {
        ApiKey.create(API_KEY_VALUE);
        Http.RequestBuilder requestBuilder = fakeRequest("POST", "/report")
                .bodyText(getFile("reportRequest.json"))
                .header("X-Api-Key", API_KEY_VALUE)
                .header("Content-Type", "application/json");

        Result result = route(requestBuilder);

        verify(elasticsearchClient).postBulk(argument.capture());
        assertEquals(10, argument.getValue().size());
        assertEquals(CREATED, result.status());
        assertEqualsString("{\"message\":\"Metrics Inserted\",\"result\":{\"hasErrors\":false,\"items\":[{\"name\":\"network_data\",\"successful\":1}]}}", result);
    }


    @Test
    public void testEmptyReport() {
        ApiKey.create(API_KEY_VALUE);
        Http.RequestBuilder requestBuilder = fakeRequest("POST", "/report")
                .bodyText(getFile("EmptyReportRequest.json"))
                .header("X-Api-Key", API_KEY_VALUE)
                .header("Content-Type", "application/json");

        Result result = route(requestBuilder);

        verify(elasticsearchClient).postBulk(argument.capture());
        assertEquals(0, argument.getValue().size());
        assertEquals(CREATED, result.status());
        assertEqualsString("{\"message\":\"Metrics Inserted\",\"result\":{\"hasErrors\":false,\"items\":[{\"name\":\"network_data\",\"successful\":1}]}}", result);
    }

    @Test
    public void testWrongAPIFormat() {
        ApiKey.create(API_KEY_VALUE);
        Http.RequestBuilder requestBuilder = fakeRequest("POST", "/report")
                .bodyText(getFile("WrongAPIFormat.json"))
                .header("X-Api-Key", API_KEY_VALUE)
                .header("Content-Type", "application/json");

        Result result = route(requestBuilder);

        assertEquals(BAD_REQUEST, result.status());
        assertThat(contentAsString(result), containsString("Unable to read class"));
    }

    @Test
    @Ignore
    public void testMalformedReportReport() {
        Http.RequestBuilder requestBuilder = fakeRequest("POST", "/report")
                .bodyText(getFile("MalformedReportRequest.json"))
                .header("Content-Type", "application/json");

        Result result = route(requestBuilder);

        assertEquals(BAD_REQUEST, result.status());
        assertTrue(contentAsJson(result).has("message"));
        assertTrue(contentAsJson(result).get("message").asText().contains("Error decoding json body"));
    }
}
