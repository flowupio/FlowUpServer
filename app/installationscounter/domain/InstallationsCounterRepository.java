package installationscounter.domain;

import installationscounter.api.InstallationsCounterApiClient;
import play.cache.CacheApi;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class InstallationsCounterRepository {

    private static final String API_KEY_CACHE_KEY = "apiKey.value.";
    private static final int CACHE_TTL = Math.toIntExact(TimeUnit.HOURS.toSeconds(1));

    private final CacheApi cache;
    private final InstallationsCounterApiClient apiClient;

    @Inject
    public InstallationsCounterRepository(CacheApi cache, InstallationsCounterApiClient apiClient) {
        this.cache = cache;
        this.apiClient = apiClient;
    }

    CompletionStage<Installation> increment(Installation installation) {
        return null;//TODO: Use the api client here to post a new installation value.
    }

    CompletionStage<Long> getInstallationCounter(String apiKey) {
        Long counter = cache.get(getApiKeyCacheKey(apiKey));
        if (counter != null) {
            return completedFuture(counter);
        }
        return apiClient.getInstallationCounter(apiKey).thenApply(counterValue -> {
            cache.set(getApiKeyCacheKey(apiKey), counterValue, CACHE_TTL);
            return counterValue;
        });
    }


    private String getApiKeyCacheKey(String apiKey) {
        return API_KEY_CACHE_KEY + apiKey;
    }
}
