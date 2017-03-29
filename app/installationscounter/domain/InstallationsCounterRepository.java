package installationscounter.domain;

import installationscounter.api.InstallationsCounterApiClient;
import play.cache.CacheApi;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class InstallationsCounterRepository {

    private static final String INSTALLATIONS_COUNTER_CACHE_KEY = "installationsCounter.value.";
    private static final int CACHE_TTL = Math.toIntExact(TimeUnit.HOURS.toSeconds(1));

    private final CacheApi cache;
    private final InstallationsCounterApiClient apiClient;

    @Inject
    public InstallationsCounterRepository(CacheApi cache, InstallationsCounterApiClient apiClient) {
        this.cache = cache;
        this.apiClient = apiClient;
    }

    public CompletionStage<Installation> increment(Installation installation) {
        return apiClient.incrementCounter(installation).thenApply(createdInstallation -> createdInstallation);
    }

    public CompletionStage<Long> getInstallationCounter(String apiKey) {
        Long counter = cache.get(getApiKeyCacheKey(apiKey));
        if (counter != null) {
            return completedFuture(counter);
        }
        return apiClient.getInstallationCounter(apiKey).thenApply(counterValue -> {
            updateInstallationsCounterCache(apiKey, counterValue);
            return counterValue;
        });
    }

    private void updateInstallationsCounterCache(String apiKey, Long counterValue) {
        cache.set(getApiKeyCacheKey(apiKey), counterValue, CACHE_TTL);
    }


    private String getApiKeyCacheKey(String apiKey) {
        return INSTALLATIONS_COUNTER_CACHE_KEY + apiKey;
    }
}
