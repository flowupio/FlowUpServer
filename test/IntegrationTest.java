import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static play.test.Helpers.*;


public class IntegrationTest {


    @Test
    public void testIndex() {
        running(testServer(3333), HTMLUNIT, browser -> {
            browser.goTo("http://localhost:3333");
            assertTrue(browser.pageSource().contains("Welcome to FlowUp"));
        });
    }
}
