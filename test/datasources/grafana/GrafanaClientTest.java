package datasources.grafana;

import apiclient.ApiClientTest;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import models.Application;
import models.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@RunWith(MockitoJUnitRunner.class)
public class GrafanaClientTest extends ApiClientTest {

    @Test
    public void doesNotChangeHomeDashboardIfThereIsNoMatchingDashboard() throws Exception {
        stubFor(get(urlEqualTo("/api/search?query&limit=100"))
                .willReturn(aResponse().withStatus(200).withBody("[]")));

        grafanaClient.updateHomeDashboard(anyUser(), anyApplication()).toCompletableFuture().get();

        verify(0, RequestPatternBuilder.newRequestPattern(RequestMethod.PUT, urlEqualTo("/api/user/preferences")));
    }

    @Test
    public void doesChangeHomeDashboardIfThereIsAMatchingDashboard() throws Exception {
        stubFor(get(urlEqualTo("/api/search?query&limit=100"))
                .willReturn(aResponse().withStatus(200).withBody(getFile("grafana/search_dashboards_response.json"))));
        stubFor(put(urlEqualTo("/api/user/preferences"))
                .willReturn(aResponse().withStatus(200).withBody("{}")));

        grafanaClient.updateHomeDashboard(anyUser(), anyApplication()).toCompletableFuture().get();

        verify(1, RequestPatternBuilder.newRequestPattern(RequestMethod.PUT, urlEqualTo("/api/user/preferences")));
    }

    private User anyUser() {
        User user = new User();
        user.setGrafanaUserId("678");
        return user;
    }

    private Application anyApplication() {
        Application application = new Application();
        application.setGrafanaOrgId("123");
        return application;
    }
}
