package controllers;

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
import static play.mvc.Http.Status.OK;
import static play.mvc.Http.Status.PRECONDITION_FAILED;
import static play.mvc.Http.Status.UNAUTHORIZED;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.route;

@RunWith(MockitoJUnitRunner.class)
public class ConfigControllerTest extends WithFlowUpApplication implements WithResources {

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
    public void returnsTheConfigAsEnabledIfTheApiKeyIsEnabled() {
        givenAnApiKey(API_KEY_VALUE, true);

        Result result = getConfig(API_KEY_VALUE);

        assertEquals(OK, result.status());
        String expect = "{\"enabled\":true}";
        assertEqualsString(expect, result);
    }

    @Test
    public void returnsTheConfigAsDisabledIfTheApiKeyIsDisabled() {
        givenAnApiKey(API_KEY_VALUE, false);

        Result result = getConfig(API_KEY_VALUE);

        assertEquals(PRECONDITION_FAILED, result.status());
    }

    @Test
    public void returnsUnauthorizedIfThereIsNoApiKeyWithTheValueSpecified() {
        Result result = getConfig(API_KEY_VALUE);

        assertEquals(UNAUTHORIZED, result.status());
    }

    private Result getConfig(String apiKey) {
        Http.RequestBuilder requestBuilder = fakeRequest("GET", "/config")
                .header("X-Api-Key", apiKey)
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
