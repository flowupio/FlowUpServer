package usecases.repositories;

import com.avaje.ebean.ExpressionList;
import datasources.database.ApiKeyDatasource;
import models.AllowedUUID;
import models.ApiKey;
import play.cache.CacheApi;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ApiKeyRepository {

    private static final int API_KEY_CACHE_TTL = (int) TimeUnit.HOURS.toSeconds(1);
    private static final int API_KEY_TODAY_ALLOWED_UUID_COUNT_TTL = (int) TimeUnit.HOURS.toSeconds(1);
    private static final int ALLOWED_UUIDS_TTL = (int) TimeUnit.HOURS.toSeconds(1);
    private static final String API_KEY_CACHE_KEY = "apiKey.value.";
    private static final String TODAY_ALLOWED_UUID_COUNT_CACHE_KEY = "apiKey.todayAllowedUUIDCount.";
    private static final String TODAY_ALLOWED_UUIDS = "allowedUUIDs.";

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

    public void addAllowedUUID(ApiKey apiKey, String uuid) {
        apiKeyDatasource.addAllowedUUID(apiKey, uuid);
        flushAllowedUUIDCache(apiKey);
    }

    private void updateApiKeyCache(ApiKey apiKey) {
        cache.set(API_KEY_CACHE_KEY + apiKey, apiKey, API_KEY_CACHE_TTL);
    }

    private void flushAllowedUUIDCache(ApiKey apiKey) {
        cache.remove(TODAY_ALLOWED_UUID_COUNT_CACHE_KEY + apiKey.getId());
        cache.remove(TODAY_ALLOWED_UUIDS + apiKey.getId());
    }

    public boolean containsAllowedUUID(ApiKey apiKey, String uuid) {
        Set<AllowedUUID> todayAllowedUUIDS = getTodayAllowedUUIDS(apiKey);
        return todayAllowedUUIDS.stream()
                .filter(allowedUUID ->
                        allowedUUID.getInstallationUUID().equals(uuid))
                .count() > 0;
    }

    public int getTodayAllowedUUIDCount(ApiKey apiKeyId) {
        return cache.getOrElse(TODAY_ALLOWED_UUID_COUNT_CACHE_KEY + apiKeyId,
                () -> getTodayAllowedUUIDQuery(apiKeyId).findRowCount(),
                API_KEY_TODAY_ALLOWED_UUID_COUNT_TTL);
    }

    public Set<AllowedUUID> getTodayAllowedUUIDS(ApiKey apiKey) {
        return cache.getOrElse(TODAY_ALLOWED_UUIDS + apiKey, () -> {
            ExpressionList<AllowedUUID> query = getTodayAllowedUUIDQuery(apiKey);
            return query.findSet();
        }, ALLOWED_UUIDS_TTL);

    }

    private ExpressionList<AllowedUUID> getTodayAllowedUUIDQuery(ApiKey apiKey) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), 0, 0, 0);
        Date today = calendar.getTime();
        calendar.add(Calendar.DATE, 1);
        Date tomorrow = calendar.getTime();
        return AllowedUUID.find.where().eq("api_key_id", apiKey.getId()).between("creation_timestamp", today, tomorrow);
    }
}
