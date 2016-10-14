import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.WithBrowser;

import static org.junit.Assert.assertTrue;


public class IntegrationTest extends WithBrowser {

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder()
                .build();
    }

    @Test
    public void testIndex() {
        browser.goTo("/");
        assertTrue(browser.pageSource().contains("Welcome to FlowUp"));
    }
}
