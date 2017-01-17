package datasources.elasticsearch;


import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import play.Configuration;
import play.libs.ws.WSClient;
import utils.WithFlowUpApplication;
import utils.WithResources;

import java.util.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class ElasticsearchClientTest extends WithFlowUpApplication implements WithResources {

    private static final String BULK_ENDPOINT = "/_bulk";
    private static final String ANY_BULK_RESPONSE = "elasticsearch/es_bulk_response.json";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    private ElasticsearchClient elasticClient;

    @Before
    public void startPlay() {
        super.startPlay();
        WSClient ws = app.injector().instanceOf(WSClient.class);
        Map<String, Object> fakeConfiguration = new HashMap<>();
        fakeConfiguration.put("scheme", "http");
        fakeConfiguration.put("host", "localhost");
        fakeConfiguration.put("port", "8080");
        Configuration elasticSearchConf = new Configuration(fakeConfiguration);
        elasticClient = new ElasticsearchClient(ws, elasticSearchConf);
    }

    @Test
    public void deleteBulkSendsAPostRequestToTheBulkEndpoint() {
        stubFor(post(urlEqualTo(BULK_ENDPOINT))
                .willReturn(aResponse()
                        .withBody(getFile(ANY_BULK_RESPONSE))
                        .withStatus(200)));

        elasticClient.deleteBulk(Collections.emptyList());
    }

    @Test
    public void postBulkWithDeleteIndexSendsTheCorrectBody() {
        stubFor(post(urlEqualTo(BULK_ENDPOINT))
                .withRequestBody(equalTo(getFile("elasticsearch/es_bulk_delete_index.json")))
                .willReturn(aResponse()
                        .withBody(getFile(ANY_BULK_RESPONSE))
                        .withStatus(200)));

        List<DeleteAction> deletes = new LinkedList<>();
        deletes.add(new DeleteAction("a", "b", "c"));
        deletes.add(new DeleteAction("d", "e", "f"));
        elasticClient.deleteBulk(deletes);
    }

}