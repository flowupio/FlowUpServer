import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.ws.WSResponse;
import usecases.MetricsDatasource;

import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static play.inject.Bindings.bind;
import static play.test.Helpers.*;


@RunWith(MockitoJUnitRunner.class)
public class IntegrationTest {

    @Mock private MetricsDatasource metricsDatasource;


    @Test
    public void testIndex() {
        Application application;
        application = new GuiceApplicationBuilder()
                .overrides(bind(MetricsDatasource.class).toInstance(metricsDatasource))
                .build();


        running(testServer(3333, application), HTMLUNIT, browser -> {
            browser.goTo("http://localhost:3333");
            assertTrue(browser.pageSource().contains("Welcome to FlowUp"));
        });
    }

    @Test
    public void testReport() {
        Application application;
        application = new GuiceApplicationBuilder()
                .overrides(bind(MetricsDatasource.class).toInstance(metricsDatasource))
                .build();

        when(metricsDatasource.writeFakeCounter()).thenReturn(CompletableFuture.completedFuture(mock(WSResponse.class)));

        running(testServer(3333, application), HTMLUNIT, browser -> {
            browser.goTo("http://localhost:3333/report");
            assertTrue(browser.pageSource().contains("Metric Inserted"));
        });
    }

}
