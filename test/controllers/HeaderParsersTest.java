package controllers;

import datasources.database.ApiKeyDatasource;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import play.cache.CacheApi;
import play.mvc.Http;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class HeaderParsersTest {


    private static final String API_KEY_VALUE = "35e25a2d1eaa464bab565f7f5e4bb029";
    
    @Test
    public void whenHeaderParsersIsCalledTwiceWithTheSameAPIKeyItShouldItTheCache() {
        ApiKeyDatasource apiKeyDatasource = givenApiKeyDatasource();
        CacheApi cacheApi = givenCacheApiForApiKey();
        HeaderParsers headerParsers = new HeaderParsers(cacheApi, apiKeyDatasource);
        Http.RequestHeader requestHeader = giventRequestHeaderWithValidApiKey();

        headerParsers.apply(requestHeader);
        headerParsers.apply(requestHeader);

        verify(apiKeyDatasource, times(1)).isValuePresentInDB(eq(API_KEY_VALUE));
    }

    @NotNull
    private ApiKeyDatasource givenApiKeyDatasource() {
        ApiKeyDatasource apiKeyDatasource = mock(ApiKeyDatasource.class);
        when(apiKeyDatasource.isValuePresentInDB(eq(API_KEY_VALUE))).thenReturn(true);
        return apiKeyDatasource;
    }

    @NotNull
    private CacheApi givenCacheApiForApiKey() {
        CacheApi cacheApi = mock(CacheApi.class);
        when(cacheApi.get(eq("apiKey.isValid." + API_KEY_VALUE))).thenReturn(null).thenReturn(true);
        return cacheApi;
    }

    @NotNull
    private Http.RequestHeader giventRequestHeaderWithValidApiKey() {
        Http.RequestHeader requestHeader = mock(Http.RequestHeader.class);
        when(requestHeader.getHeader(eq(HeaderParsers.X_API_KEY))).thenReturn(API_KEY_VALUE);
        return requestHeader;
    }

}