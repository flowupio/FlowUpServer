package usecases.repositories;

import datasources.database.ApiKeyDatasource;
import models.ApiKey;
import play.cache.CacheApi;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.concurrent.TimeUnit;

public class ApiKeyRepository {

    private static final int API_KEY_CACHE_TTL = (int) TimeUnit.HOURS.toSeconds(1);
    private static final String API_KEY_CACHE_KEY = "apiKey.value.";

    private CacheApi cache;
    private ApiKeyDatasource apiKeyDatasource;

    @Inject
    public ApiKeyRepository(CacheApi cache, ApiKeyDatasource apiKeyDatasource) {
        this.cache = cache;
        this.apiKeyDatasource = apiKeyDatasource;
    }

    @NotNull
    public ApiKey create() {
        ApiKey apiKey = apiKeyDatasource.create();
        updateApiKeyCache(apiKey);
        return apiKey;
    }

    @NotNull
    public ApiKey create(String value) {
        return create(value, true);
    }

    @NotNull
    public ApiKey create(String value, boolean enabled) {
        ApiKey apiKey = apiKeyDatasource.create(value, enabled);
        updateApiKeyCache(apiKey);
        return apiKey;
    }

    @Nullable
    public ApiKey getApiKey(String apiKey) {
        return cache.getOrElse(API_KEY_CACHE_KEY + apiKey,
                () -> apiKeyDatasource.findByApiKeyValue(apiKey),
                API_KEY_CACHE_TTL);
    }

    private void updateApiKeyCache(ApiKey apiKey) {
        cache.set(API_KEY_CACHE_KEY + apiKey, apiKey, API_KEY_CACHE_TTL);
    }
}
