package datasources.elasticsearch;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.Configuration;
import play.libs.ws.WSClient;
import utils.Time;
import utils.WithFlowUpApplication;
import utils.WithResources;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ElasticsearchClientTest extends WithFlowUpApplication implements WithResources {

    private final static String ANY_INDEX = "index1";
    private static final String GET_INDEXES_PATH = "/_cat/indices?v";
    private static final String SEARCH_ENDPOINT = "/_search";
    private static final String ANY_GET_OLD_DATAPOINTS_RESPONSE = "elasticsearch/es_get_old_datapoints_response.json";
    private static final long ANY_TIMESTAMP = 1482248233017L;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();
    @Mock private Time time;

    private ElasticsearchClient apiClient;

    @Before
    public void startPlay() {
        super.startPlay();
        WSClient ws = app.injector().instanceOf(WSClient.class);
        Map<String, Object> elasticConfig = new HashMap<>();
        elasticConfig.put("scheme", "http");
        elasticConfig.put("host", "localhost");
        elasticConfig.put("port", "8080");
        elasticConfig.put("documents_ttl_in_days", "10");
        Configuration config = new Configuration(elasticConfig);
        when(time.daysAgo(anyInt())).thenReturn(new DateTime(ANY_TIMESTAMP));
        apiClient = new ElasticsearchClient(ws, config, time);
    }

    @Test
    public void sendsTheDeleteIndexRequestToTheCorrectPathUsingADeleteMethod() throws Exception {
        stubFor(delete(urlEqualTo("/" + ANY_INDEX))
                .willReturn(aResponse()
                        .withStatus(200)));

        apiClient.deleteIndex(ANY_INDEX).toCompletableFuture().get();
    }

    @Test
    public void sendsTheGetRequestIndexToTheCorrectPathUsingAGetMethod() throws Exception {
        stubFor(get(urlEqualTo(GET_INDEXES_PATH))
                .willReturn(aResponse()
                        .withStatus(200)));

        apiClient.getIndexes().toCompletableFuture().get();
    }

    @Test
    public void parsesTheIndexesProperly() throws Exception {
        stubFor(get(urlEqualTo(GET_INDEXES_PATH))
                .willReturn(aResponse()
                        .withBody(getFile("elasticsearch/es_get_indexes_response"))
                        .withStatus(200)));

        List<Index> indices = apiClient.getIndexes().toCompletableFuture().get();

        assertEquals(2, indices.size());
    }

    @Test
    public void parsesAnEmptyListOfIndexesProperly() throws Exception {
        stubFor(get(urlEqualTo(GET_INDEXES_PATH))
                .willReturn(aResponse()
                        .withBody(getFile("elasticsearch/es_get_indexes_empty_response"))
                        .withStatus(200)));

        List<Index> indices = apiClient.getIndexes().toCompletableFuture().get();

        assertTrue(indices.isEmpty());
    }

    @Test
    public void sendsTheGetOldDataPointsToTheSearchPathUsingAGetMethod() throws Exception {
        stubFor(post(urlEqualTo(SEARCH_ENDPOINT))
                .willReturn(aResponse()
                        .withBody(getFile(ANY_GET_OLD_DATAPOINTS_RESPONSE))
                        .withStatus(200)));

        apiClient.getOldDataPoints().toCompletableFuture().get();
    }

    @Test
    public void parsesTheOldDataPointsProperly() throws Exception {
        stubFor(post(urlEqualTo(SEARCH_ENDPOINT))
                .willReturn(aResponse()
                        .withBody(getFile(ANY_GET_OLD_DATAPOINTS_RESPONSE))
                        .withStatus(200)));

        SearchResponse searchResponse = apiClient.getOldDataPoints().toCompletableFuture().get();

        assertEquals(4, searchResponse.getHits().getTotal());
        assertEquals(2, searchResponse.getHits().getHits().size());
    }

    @Test
    public void parsesTheOldDataPointsProperlyEvenIfTheSearchDoesNotReturnHits() throws Exception {
        stubFor(post(urlEqualTo(SEARCH_ENDPOINT))
                .willReturn(aResponse()
                        .withBody(getFile("elasticsearch/es_get_old_datapoints_empty_response.json"))
                        .withStatus(200)));

        SearchResponse searchResponse = apiClient.getOldDataPoints().toCompletableFuture().get();

        assertTrue(searchResponse.getHits().getHits().isEmpty());
        assertEquals(0, searchResponse.getHits().getTotal());
    }

    @Test public void getOldDataPointsUsesTheSearchEndpointWithA10000SizeSearch() throws Exception {
        stubFor(post(urlEqualTo(SEARCH_ENDPOINT))
                .withRequestBody(equalToJson(getFile("elasticsearch/es_get_old_datapoints_request.json")))
                .willReturn(aResponse()
                        .withBody(getFile(ANY_GET_OLD_DATAPOINTS_RESPONSE))
                        .withStatus(200)));

        apiClient.getOldDataPoints().toCompletableFuture().get();
    }

}