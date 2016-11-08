package controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import utils.WithFlowUpApplication;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.route;

public class HomeControllerTest extends WithFlowUpApplication {
    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder()
                .configure((Map) Helpers.inMemoryDatabase("default", ImmutableMap.of(
                        "MODE", "MYSQL"
                )))
                .build();
    }

    @Test
    public void health() throws Exception {
        Http.RequestBuilder requestBuilder = fakeRequest("GET", "/health");

        Result result = route(requestBuilder);

        assertEquals(OK, result.status());
    }
}
