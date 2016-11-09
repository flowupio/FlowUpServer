package controllers;

import datasources.database.ApiKeyDatasource;
import models.ApiKey;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.cache.CacheApi;
import play.mvc.Http;
import play.test.WithApplication;
import repositories.ApiKeyRepository;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class HeaderParsersTest extends WithApplication {

    private static final String API_KEY_VALUE = "35e25a2d1eaa464bab565f7f5e4bb029";

    private CacheApi cacheApi;

    @Before
    public void startPlay() {
        super.startPlay();
        cacheApi = app.injector().instanceOf(CacheApi.class);
        cacheApi = spy(cacheApi);
    }

    @After
    public void stopPlay() {
        cacheApi.remove("apiKey.value." + API_KEY_VALUE);
        super.stopPlay();
    }

    @Test
    public void whenHeaderParsersIsCalledTwiceWithTheSameAPIKeyItShouldItTheCache() {
        ApiKeyDatasource apiKeyDatasource = givenApiKeyDatasource();
        HeaderParsers headerParsers = new HeaderParsers(new ApiKeyRepository(cacheApi, apiKeyDatasource));
        Http.RequestHeader requestHeader = givenRequestHeaderWithValidApiKey();

        headerParsers.apply(requestHeader);
        headerParsers.apply(requestHeader);

        verify(apiKeyDatasource, times(1)).findByApiKeyValue(eq(API_KEY_VALUE));
    }


    @NotNull
    private ApiKeyDatasource givenApiKeyDatasource() {
        ApiKeyDatasource apiKeyDatasource = mock(ApiKeyDatasource.class);
        when(apiKeyDatasource.findByApiKeyValue(eq(API_KEY_VALUE))).thenReturn(new ApiKey());
        return apiKeyDatasource;
    }

    @NotNull
    private Http.RequestHeader givenRequestHeaderWithValidApiKey() {
        Http.RequestHeader requestHeader = mock(Http.RequestHeader.class);
        when(requestHeader.getHeader(eq(HeaderParsers.X_API_KEY))).thenReturn(API_KEY_VALUE);
        return requestHeader;
    }

}