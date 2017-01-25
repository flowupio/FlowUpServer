package controllers.api;

import models.ApiKey;
import models.Platform;
import models.Version;
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

import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static play.inject.Bindings.bind;
import static play.mvc.Http.Status.OK;
import static play.mvc.Http.Status.UNAUTHORIZED;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.route;

@RunWith(MockitoJUnitRunner.class)
public class ConfigControllerTest extends WithFlowUpApplication implements WithResources {

    private static final String API_KEY_VALUE = "35e25a2d1eaa464bab565f7f5e4bb029";
    private static final Version VERSION_1 = new Version(0, 0, 1, Platform.ANDROID);
    private static final Version VERSION_2 = new Version(2, 0, 0, Platform.ANDROID);

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

        assertEquals(OK, result.status());
    }

    @Test
    public void returnsTheConfigAsDisabledIfTheVersionIsNotSupported() {
        givenAnApiKey(API_KEY_VALUE, true, VERSION_2);

        Result result = getConfig(API_KEY_VALUE, VERSION_1.toString());

        assertConfigIsDisabled(result);
    }

    @Test
    public void returnsTheConfigAsIsIfTheVersionIsTheSame() {
        givenAnApiKey(API_KEY_VALUE, false, VERSION_1);

        Result result = getConfig(API_KEY_VALUE, VERSION_1.toString());

        assertConfigIsDisabled(result);
    }

    @Test
    public void returnsTheConfigAsIsIfTheVersionIsBigger() {
        givenAnApiKey(API_KEY_VALUE, false, VERSION_1);

        Result result = getConfig(API_KEY_VALUE, VERSION_2.toString());

        assertConfigIsDisabled(result);
    }

    @Test
    public void returnsUnauthorizedIfThereIsNoApiKeyWithTheValueSpecified() {
        givenThereIsNoApiKey(API_KEY_VALUE);

        Result result = getConfig(API_KEY_VALUE);

        assertEquals(UNAUTHORIZED, result.status());
    }

    private void assertConfigIsDisabled(Result result) {
        assertEquals(OK, result.status());
        String expect = "{\"enabled\":false}";
        assertEqualsString(expect, result);
    }

    private Result getConfig(String apiKey) {
        return getConfig(apiKey, VERSION_2.toString());
    }

    private Result getConfig(String apiKey, String userAgent) {
        Http.RequestBuilder requestBuilder = fakeRequest("GET", "/config")
                .header("X-Api-Key", apiKey)
                .header("X-UUID", "anyUUID")
                .header("Content-Type", "application/json")
                .header("User-Agent", userAgent);
        return route(requestBuilder);
    }

    private void givenAnApiKey(String apiKeyValue, boolean enabled) {
        givenAnApiKey(apiKeyValue, enabled, VERSION_1);
    }

    private void givenAnApiKey(String apiKeyValue, boolean enabled, Version minSdkVersionSupported) {
        ApiKey apiKey = new ApiKey();
        apiKey.setValue(apiKeyValue);
        apiKey.setEnabled(enabled);
        apiKey.setMinAndroidSDKSupported(minSdkVersionSupported.toString());
        when(apiKeyRepository.getApiKey(apiKeyValue)).thenReturn(apiKey);
        when(apiKeyRepository.getApiKeyAsync(apiKeyValue)).thenReturn(CompletableFuture.completedFuture(apiKey));
    }

    private void givenThereIsNoApiKey(String apiKeyValue) {
        when(apiKeyRepository.getApiKey(apiKeyValue)).thenReturn(null);
        when(apiKeyRepository.getApiKeyAsync(apiKeyValue)).thenReturn(CompletableFuture.completedFuture(null));
    }
}
