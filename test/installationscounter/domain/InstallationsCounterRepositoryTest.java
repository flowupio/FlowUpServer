package installationscounter.domain;

import installationscounter.api.InstallationsCounterApiClient;
import models.Version;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.cache.CacheApi;
import utils.Time;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InstallationsCounterRepositoryTest {

    private static final String ANY_API_KEY = "bec94b0431354ba0bcf0b996960cd262";
    private static final String ANY_UUID = UUID.randomUUID().toString();
    private static final Version ANY_VERSION = Version.fromString("FlowUpAndroidSDK/0.3.0");
    private static final Installation ANY_INSTALLATION = new Installation(ANY_API_KEY, ANY_UUID, ANY_VERSION, new Time().now().getMillis());
    private static final Long ANY_COUNTER_VALUE = 11L;
    private static final int CACHE_TTL = Math.toIntExact(TimeUnit.HOURS.toSeconds(1));

    private InstallationsCounterRepository repository;
    @Mock
    private CacheApi cache;
    @Mock
    private InstallationsCounterApiClient apiClient;

    @Before
    public void setUp() {
        repository = new InstallationsCounterRepository(cache, apiClient);
    }

    @Test
    public void returnsTheInstallationObtainedFromTheApiClient() throws Exception {
        givenTheInstallationIsPersisted(ANY_INSTALLATION);

        Installation result = repository.increment(ANY_INSTALLATION).toCompletableFuture().get();

        assertEquals(ANY_INSTALLATION, result);
    }

    @Test
    public void returnsTheCounterObtainedFromTheCache() throws Exception {
        givenThereIsAnInstallationsCounterCached(ANY_API_KEY, ANY_COUNTER_VALUE);

        Long result = repository.getInstallationCounter(ANY_API_KEY).toCompletableFuture().get();

        assertEquals(ANY_COUNTER_VALUE, result);
    }

    @Test
    public void returnsTheCounterValueObtainedFromTheApiClient() throws Exception {
        givenThereIsAnInstallationsCounterCached(ANY_API_KEY, null);
        givenThereIsAnInstallationCounterInTheApi(ANY_API_KEY, ANY_COUNTER_VALUE);

        Long result = repository.getInstallationCounter(ANY_API_KEY).toCompletableFuture().get();

        assertEquals(ANY_COUNTER_VALUE, result);
    }

    @Test
    public void updatesTheCachedCounterAfterGettingItFromTheApiClient() throws Exception {
        givenThereIsAnInstallationsCounterCached(ANY_API_KEY, null);
        givenThereIsAnInstallationCounterInTheApi(ANY_API_KEY, ANY_COUNTER_VALUE);

        repository.getInstallationCounter(ANY_API_KEY).toCompletableFuture().get();

        verify(cache).set(getCacheKey(ANY_API_KEY), ANY_COUNTER_VALUE, CACHE_TTL);
    }

    private void givenThereIsAnInstallationCounterInTheApi(String apiKey, Long value) {
        when(apiClient.getInstallationCounter(apiKey)).thenReturn(completedFuture(value));
    }

    private void givenThereIsAnInstallationsCounterCached(String apiKey, Long counterValue) {
        when(cache.get(getCacheKey(apiKey))).thenReturn(counterValue);
    }

    @NotNull
    private String getCacheKey(String apiKey) {
        return "installationsCounter.value." + apiKey;
    }

    private void givenTheInstallationIsPersisted(Installation installation) {
        when(apiClient.incrementCounter(installation)).thenReturn(completedFuture(installation));
    }

}