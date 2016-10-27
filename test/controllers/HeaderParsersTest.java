package controllers;

import com.google.common.collect.ImmutableMap;
import datasources.database.ApiDatasource;
import datasources.elasticsearch.ElasticsearchClient;
import models.ApiKey;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import play.Application;
import play.cache.CacheApi;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;
import play.test.Helpers;
import play.test.WithApplication;

import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static play.inject.Bindings.bind;

public class HeaderParsersTest {


    private static final String API_KEY_VALUE = "35e25a2d1eaa464bab565f7f5e4bb029";
    
    @Test
    public void whenHeaderParsersIsCalledTwiceWithTheSameAPIKeyItShouldItTheCache() {
        ApiDatasource apiDatasource = mock(ApiDatasource.class);
        when(apiDatasource.isValuePresentInDB(eq(API_KEY_VALUE))).thenReturn(true);
        CacheApi cacheApi = givenCacheApiForApiKey();
        HeaderParsers headerParsers = new HeaderParsers(cacheApi, apiDatasource);
        Http.RequestHeader requestHeader = giventRequestHeaderWithValidApiKey();

        headerParsers.apply(requestHeader);
        headerParsers.apply(requestHeader);

        verify(apiDatasource, times(1)).isValuePresentInDB(eq(API_KEY_VALUE));
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