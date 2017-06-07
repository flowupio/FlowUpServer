package datasources.grafana;

import apiclient.ApiClientTest;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import models.Application;
import models.Platform;
import models.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import usecases.models.Dashboard;

import java.util.Collections;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GrafanaClientTest extends ApiClientTest {

    @Test
    public void doesNotChangeHomeDashboardIfThereIsNoMatchingDashboard() throws Exception {
        givenThereAreNoDashboardsInGrafana();

        grafanaClient.updateHomeDashboard(anyUser(), anyApplication()).toCompletableFuture().get();

        verify(0, RequestPatternBuilder.newRequestPattern(RequestMethod.PUT, urlEqualTo("/api/user/preferences")));
    }

    @Test
    public void doesChangeHomeDashboardIfThereIsAMatchingDashboard() throws Exception {
        givenThereAreSomeDashboardsInGrafana();
        stubFor(put(urlEqualTo("/api/user/preferences"))
                .willReturn(aResponse().withStatus(200).withBody("{}")));

        grafanaClient.updateHomeDashboard(anyUser(), anyApplication()).toCompletableFuture().get();

        verify(1, RequestPatternBuilder.newRequestPattern(RequestMethod.PUT, urlEqualTo("/api/user/preferences")));
    }

    @Test
    public void doesNotCreateDashboardsIfThereAreAlreadyDashboards() throws Exception {
        givenThereAreSomeDashboardsInGrafana();

        grafanaClient.createDashboards(anyApplication(), Platform.ANDROID).toCompletableFuture().get();

        verify(0, RequestPatternBuilder.newRequestPattern(RequestMethod.POST, urlEqualTo("/api/dashboards/db")));
    }

    @Test
    public void doesCreateDashboardsIfThereAreNoDashboards() throws Exception {
        givenThereAreDashboardsToUpload();
        givenThereAreNoDashboardsInGrafana();
        stubFor(post(urlEqualTo("/api/dashboards/db"))
                .willReturn(aResponse().withStatus(200).withBody("{\"slug\":\"home\",\"status\":\"success\",\"version\":0}")));

        grafanaClient.createDashboards(anyApplication(), Platform.ANDROID).toCompletableFuture().get();

        verify(1, RequestPatternBuilder.newRequestPattern(RequestMethod.POST, urlEqualTo("/api/dashboards/db")));
    }

    private void givenThereAreSomeDashboardsInGrafana() {
        stubFor(get(urlEqualTo("/api/search?query&limit=100"))
                .willReturn(aResponse().withStatus(200).withBody(getFile("grafana/search_dashboards_response.json"))));
    }

    private void givenThereAreNoDashboardsInGrafana() {
        stubFor(get(urlEqualTo("/api/search?query&limit=100"))
                .willReturn(aResponse().withStatus(200).withBody("[]")));
    }

    private void givenThereAreDashboardsToUpload() {
        when(dashboardsDataSource.getDashboards(any())).thenReturn(Collections.singletonList(new Dashboard("{}")));
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
