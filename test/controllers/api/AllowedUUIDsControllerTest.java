package controllers.api;

import models.ApiKey;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;
import play.mvc.Result;
import usecases.repositories.ApiKeyRepository;
import utils.WithFlowUpApplication;
import utils.WithResources;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static play.inject.Bindings.bind;
import static play.mvc.Http.Status.*;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.route;

@RunWith(MockitoJUnitRunner.class)
public class AllowedUUIDsControllerTest extends WithFlowUpApplication implements WithResources {

    private static final String API_KEY_VALUE = "35e25a2d1eaa464bab565f7f5e4bb029";

    @Mock
    private ApiKeyRepository apiKeyRepository;

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder()
                .overrides(bind(ApiKeyRepository.class).toInstance(apiKeyRepository))
                .build();
    }

    @Test
    public void returnsOkIfThereAreApiKeys() {
        givenAnApiKey(API_KEY_VALUE, true);

        Result result = deleteYesterdayAllowedUUIDs();

        assertEquals(OK, result.status());
    }

    @Test
    public void returnsOkIfThereAreNoApiKeys() {
        Result result = deleteYesterdayAllowedUUIDs();

        assertEquals(OK, result.status());
    }

    private Result deleteYesterdayAllowedUUIDs() {
        Http.RequestBuilder requestBuilder = fakeRequest("DELETE", "/allowedUUIDs")
                .header("Content-Type", "application/json");
        return route(requestBuilder);
    }

    private void givenAnApiKey(String apiKeyValue, boolean enabled) {
        ApiKey apiKey = new ApiKey();
        apiKey.setValue(apiKeyValue);
        apiKey.setEnabled(enabled);
        when(apiKeyRepository.getApiKey(apiKeyValue)).thenReturn(apiKey);
    }

}
