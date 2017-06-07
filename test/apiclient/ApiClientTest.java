package apiclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import datasources.elasticsearch.ElasticsearchClient;
import datasources.grafana.DashboardsDataSource;
import datasources.grafana.GrafanaClient;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.Configuration;
import play.libs.ws.WSClient;
import usecases.mapper.DashboardMapper;
import utils.Time;
import utils.WithFlowUpApplication;
import utils.WithResources;

import java.util.HashMap;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public abstract class ApiClientTest extends WithFlowUpApplication implements WithResources {

    protected ElasticsearchClient elasticsearchClient;
    protected GrafanaClient grafanaClient;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();
    @Mock
    protected Time time;

    @Before
    public void startPlay() {
        super.startPlay();
        WSClient ws = app.injector().instanceOf(WSClient.class);
        Map<String, Object> elasticConfigValues = new HashMap<>();
        elasticConfigValues.put("scheme", "http");
        elasticConfigValues.put("host", "localhost");
        elasticConfigValues.put("port", "8080");
        elasticConfigValues.put("documents_ttl_in_days", "10");

        Map<String, Object> grafanaConfigValues = new HashMap<>();
        grafanaConfigValues.put("api_key", "grafana api key");
        grafanaConfigValues.put("admin_user", "I am an administrator");
        grafanaConfigValues.put("admin_password", "I am a password");
        grafanaConfigValues.put("scheme", "http");
        grafanaConfigValues.put("host", "localhost");
        grafanaConfigValues.put("port", "8080");
        Configuration elasticConfig = new Configuration(elasticConfigValues);
        Configuration grafanaConfig = new Configuration(grafanaConfigValues);
        elasticsearchClient = new ElasticsearchClient(ws, elasticConfig, time);

        grafanaClient = new GrafanaClient(
                ws,
                grafanaConfig,
                elasticConfig,
                new DashboardMapper(new ObjectMapper()),
                app.injector().instanceOf(DashboardsDataSource.class));
    }
}
