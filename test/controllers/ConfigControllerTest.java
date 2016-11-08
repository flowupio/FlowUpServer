package controllers;

import datasources.database.ApiKeyDatasource;
import datasources.database.OrganizationDatasource;
import models.ApiKey;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;
import play.mvc.Result;
import repositories.ApiKeyRepository;
import utils.WithFlowUpApplication;
import utils.WithResources;

import static org.junit.Assert.assertEquals;
import static play.inject.Bindings.bind;
import static play.mvc.Http.Status.OK;
import static play.mvc.Http.Status.UNAUTHORIZED;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.route;

@RunWith(MockitoJUnitRunner.class)
public class ConfigControllerTest extends WithFlowUpApplication implements WithResources {

    private static final String API_KEY_VALUE = "35e25a2d1eaa464bab565f7f5e4bb029";

    @Mock
    private ApiKeyRepository apiKeyRepository;
    private ApiKeyDatasource apiKeyDatasource;

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder()
                .overrides(bind(ApiKeyRepository.class).toInstance(apiKeyRepository))
                .build();
    }

    @Before
    @Override
    public void startPlay() {
        super.startPlay();
        apiKeyDatasource = new ApiKeyDatasource();
    }

    @Test
    public void returnsTheConfigAsEnabledIfTheApiKeyIsEnabled() {
        givenAnApiKey(API_KEY_VALUE, true);
        Http.RequestBuilder requestBuilder = getConfig();

        Result result = route(requestBuilder);

        assertEquals(OK, result.status());
        String expect = "{\"enabled\":\"true\"}";
        assertEqualsString(expect, result);
    }

    @Test
    public void returnsTheConfigAsDisabledIfTheApiKeyIsDisabled() {
        givenAnApiKey(API_KEY_VALUE, false);
        Http.RequestBuilder requestBuilder = getConfig();

        Result result = route(requestBuilder);

        assertEquals(OK, result.status());
        String expect = "{\"enabled\":\"false\"}";
        assertEqualsString(expect, result);
    }

    @Test
    public void returnsUnauthorizedIfThereIsNoApiKeyWithTheValueSpecified() {
        givenAnApiKey(API_KEY_VALUE, false);
        Http.RequestBuilder requestBuilder = getConfig();

        Result result = route(requestBuilder);

        assertEquals(UNAUTHORIZED, result.status());
    }

    private Http.RequestBuilder getConfig() {
        return fakeRequest("GET", "/config")
                .header("X-Api-Key", API_KEY_VALUE)
                .header("Content-Type", "application/json");
    }

    private void givenAnApiKey(String apiKeyValue, boolean enabled) {
        ApiKey apiKey = apiKeyDatasource.create(apiKeyValue, enabled);
        new OrganizationDatasource(apiKeyDatasource).create("example", "@example.com", apiKey);
    }

}
