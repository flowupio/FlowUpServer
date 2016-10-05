import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.ws.WSResponse;
import usecases.MetricsDatasource;

import java.util.concurrent.CompletionStage;

import static org.junit.Assert.assertTrue;
import static play.inject.Bindings.bind;
import static play.test.Helpers.*;

class MockMetricsDatasource implements MetricsDatasource {


    @Override
    public CompletionStage<WSResponse> writeFakeCounter() {
        return null;
    }
}


public class IntegrationTest {

    /**
     * add your integration test here
     * in this example we just check if the welcome page is being shown
     */
    @Test
    public void test() {
        Application application;
        application = new GuiceApplicationBuilder()
                .overrides(bind(MetricsDatasource.class).to(MockMetricsDatasource.class))
                .build();

        running(testServer(3333, application), HTMLUNIT, browser -> {
            browser.goTo("http://localhost:3333");
            assertTrue(browser.pageSource().contains("Metric Inserted"));
        });
    }

}
