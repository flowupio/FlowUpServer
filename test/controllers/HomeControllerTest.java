package controllers;

import org.junit.Test;
import play.mvc.Http;
import play.mvc.Result;
import utils.WithFlowUpApplication;


import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.route;

public class HomeControllerTest extends WithFlowUpApplication {

    @Test
    public void health() throws Exception {
        Http.RequestBuilder requestBuilder = fakeRequest("GET", "/health");

        Result result = route(requestBuilder);

        assertEquals(OK, result.status());
    }
}
