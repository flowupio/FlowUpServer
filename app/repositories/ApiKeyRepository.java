package repositories;

import datasources.database.ApiKeyDatasource;
import models.ApiKey;
import play.cache.CacheApi;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

public class ApiKeyRepository {

    private static final int API_KEY_CACHE_TTL = (int) TimeUnit.HOURS.toSeconds(1);
    public static final String API_KEY_CACHE_KEY = "apiKey.value.";

    private CacheApi cache;
    private ApiKeyDatasource apiKeyDatasource;

    @Inject
    public ApiKeyRepository(CacheApi cache, ApiKeyDatasource apiKeyDatasource) {
        this.cache = cache;
        this.apiKeyDatasource = apiKeyDatasource;
    }

    @Nullable
    public ApiKey getApiKey(String apiKey) {
        return cache.getOrElse(API_KEY_CACHE_KEY + apiKey,
                () -> apiKeyDatasource.findByApiKeyValue(apiKey),
                API_KEY_CACHE_TTL);
    }

}
