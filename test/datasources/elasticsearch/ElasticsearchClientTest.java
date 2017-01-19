package datasources.elasticsearch;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import play.Configuration;
import play.libs.ws.WSClient;
import utils.WithFlowUpApplication;
import utils.WithResources;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ElasticsearchClientTest extends WithFlowUpApplication implements WithResources {

    private final static String ANY_INDEX = "index1";
    private static final String GET_INDEXES_PATH = "/_cat/indices?v";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

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
        apiClient = new ElasticsearchClient(ws, config);
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

    @Test public void parsesTheIndexesProperly() throws Exception {
        stubFor(get(urlEqualTo(GET_INDEXES_PATH))
                .willReturn(aResponse()
                        .withBody(getFile("elasticsearch/es_get_indexes_response"))
                        .withStatus(200)));

        List<Index> indices = apiClient.getIndexes().toCompletableFuture().get();

        assertEquals(2, indices.size());
    }

    @Test public void parsesAnEmptyListOfIndexesProperly() throws Exception {
        stubFor(get(urlEqualTo(GET_INDEXES_PATH))
                .willReturn(aResponse()
                        .withBody(getFile("elasticsearch/es_get_indexes_empty_response"))
                        .withStatus(200)));

        List<Index> indices = apiClient.getIndexes().toCompletableFuture().get();

        assertTrue(indices.isEmpty());
    }

}