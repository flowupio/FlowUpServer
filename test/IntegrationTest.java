import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.Helpers;
import play.test.WithBrowser;

import java.util.Map;

import static org.junit.Assert.assertTrue;


public class IntegrationTest extends WithBrowser {

    @Override
    protected Application provideApplication() {

        return new GuiceApplicationBuilder()
                .configure((Map) Helpers.inMemoryDatabase())
                .build();
    }

    @Test
    public void testIndex() {
        browser.goTo("/");
        assertTrue(browser.pageSource().contains("Welcome to FlowUp"));
    }
}
