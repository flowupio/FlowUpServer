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
import usecases.models.ErrorReport;
import usecases.repositories.ApiKeyRepository;
import utils.WithFlowUpApplication;
import utils.WithResources;

import java.util.concurrent.CompletableFuture;

import static play.mvc.Http.Status.CREATED;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static play.inject.Bindings.bind;
import static play.mvc.Http.Status.UNAUTHORIZED;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.route;

import play.libs.Json;

@RunWith(MockitoJUnitRunner.class)
public class CrashReporterControllerTest extends WithFlowUpApplication implements WithResources {

    private static final String API_KEY_VALUE = "35e25a2d1eaa464bab565f7f5e4bb029";
    private static final Version ANY_VERSION = new Version(2, 0, 0, Platform.ANDROID);

    @Mock
    private ApiKeyRepository apiKeyRepository;

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder()
                .overrides(bind(ApiKeyRepository.class).toInstance(apiKeyRepository))
                .build();
    }

    @Test
    public void returnsTheErrorReportAsCreatedIfTheApiKeyIsValid() {
        givenAnApiKey(API_KEY_VALUE);

        Result result = reportError(API_KEY_VALUE);

        ErrorReport errorReport = new ErrorReport("Nexus-5X",
                "API23",
                true,
                "java.lang.NullPointerException: Crash collecting data",
                "java.lang.NullPointerException: Crash collecting data at Declaring class.method name(file name:11)");
        String expect = Json.toJson(errorReport).toString();
        assertEquals(CREATED, result.status());
        assertEqualsString(expect, result);
    }

    @Test
    public void returnsUnauthorizedWhenReportingAnErrorIfThereIsNoApiKeyWithTheValueSpecified() {
        givenThereIsNoApiKey(API_KEY_VALUE);

        Result result = reportError(API_KEY_VALUE);

        assertEquals(UNAUTHORIZED, result.status());
    }

    private Result reportError(String apiKey) {
        Http.RequestBuilder requestBuilder = fakeRequest("POST", "/errorReport")
                .bodyText(getFile("crashreporter/reportErrorRequest.json"))
                .header("X-Api-Key", apiKey)
                .header("X-UUID", "anyUUID")
                .header("Content-Type", "application/json")
                .header("User-Agent", ANY_VERSION.toString());
        return route(requestBuilder);
    }

    private void givenAnApiKey(String apiKeyValue) {
        givenAnApiKey(apiKeyValue, true, ANY_VERSION);
    }

    private void givenAnApiKey(String apiKeyValue, boolean enabled, Version minSdkVersionSupported) {
        ApiKey apiKey = new ApiKey();
        apiKey.setEnabled(enabled);
        apiKey.setValue(apiKeyValue);
        apiKey.setMinAndroidSDKSupported(minSdkVersionSupported.toString());
        when(apiKeyRepository.getApiKey(apiKeyValue)).thenReturn(apiKey);
        when(apiKeyRepository.getApiKeyAsync(apiKeyValue)).thenReturn(CompletableFuture.completedFuture(apiKey));
    }

    private void givenThereIsNoApiKey(String apiKeyValue) {
        when(apiKeyRepository.getApiKey(apiKeyValue)).thenReturn(null);
        when(apiKeyRepository.getApiKeyAsync(apiKeyValue)).thenReturn(CompletableFuture.completedFuture(null));
    }
}
