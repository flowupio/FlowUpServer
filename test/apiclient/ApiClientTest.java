package apiclient;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import datasources.elasticsearch.ElasticsearchClient;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.Configuration;
import play.libs.ws.WSClient;
import utils.Time;
import utils.WithFlowUpApplication;
import utils.WithResources;

import java.util.HashMap;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public abstract class ApiClientTest extends WithFlowUpApplication implements WithResources {

    protected ElasticsearchClient elasticsearchClient;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();
    @Mock
    protected Time time;

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
        elasticsearchClient = new ElasticsearchClient(ws, config, time);
    }
}
