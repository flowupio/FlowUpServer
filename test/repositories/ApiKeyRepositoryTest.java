package repositories;

import models.AllowedUUID;
import models.ApiKey;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.Application;
import play.cache.CacheApi;
import play.inject.Injector;
import play.inject.guice.GuiceApplicationBuilder;
import usecases.repositories.ApiKeyRepository;
import utils.Time;
import utils.WithFlowUpApplication;

import java.util.Set;
import java.util.UUID;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static play.inject.Bindings.bind;

@RunWith(MockitoJUnitRunner.class)
public class ApiKeyRepositoryTest extends WithFlowUpApplication {

    private static final String ANY_UUID = "abcd";
    private static final String ANY_OTHER_UUID = "efgh";
    private static final String ANY_API_KEY = "12345";

    private ApiKeyRepository apiKeyRepository;
    private CacheApi cache;
    private UUID apiKeyId;
    @Mock
    private Time time = new Time();
    private Time defaultTime = new Time();

    @Before
    @Override
    public void startPlay() {
        super.startPlay();
        Injector injector = app.injector();
        apiKeyRepository = injector.instanceOf(ApiKeyRepository.class);
        cache = spy(injector.instanceOf(CacheApi.class));
        configureDefaultTime();
    }

    private void configureDefaultTime() {
        givenTodayIsToday();
    }

    @After
    @Override
    public void stopPlay() {
        cache.remove("apiKey.todayAllowedUUIDCount." + apiKeyId);
        cache.remove("allowedUUIDs." + apiKeyId);
        super.stopPlay();
    }

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder()
                .overrides(bind(Time.class).toInstance(time))
                .build();
    }

    @Test
    public void addsTheNewAllowedUUIDToTheApiKey() {
        ApiKey originalApiKey = givenAnApiKey();

        apiKeyRepository.addAllowedUUID(originalApiKey, ANY_UUID);
        ApiKey apiKey = apiKeyRepository.getApiKey(originalApiKey.getValue());

        assertTrue(apiKeyRepository.containsAllowedUUID(apiKey, ANY_UUID));
    }

    @Test
    public void doesNotContainAnUUIDAddedYesterday() {
        givenTodayIsYesterday();
        ApiKey apiKey = givenAnApiKey();

        apiKeyRepository.addAllowedUUID(apiKey, ANY_UUID);

        assertFalse(apiKeyRepository.containsAllowedUUID(apiKey, ANY_UUID));
    }

    @Test
    public void containsAnUUIDIfHasBeenAddedToday() {
        ApiKey apiKey = givenAnApiKey();

        apiKeyRepository.addAllowedUUID(apiKey, ANY_UUID);
        apiKeyRepository.addAllowedUUID(apiKey, ANY_OTHER_UUID);

        Set<AllowedUUID> allowedUUIDs = apiKeyRepository.getTodayAllowedUUIDS(apiKey);
        long numberOfAllowedUUIDs = allowedUUIDs.stream().filter(allowedUUID -> {
            String installationUUID = allowedUUID.getInstallationUUID();
            return installationUUID.equals(ANY_UUID) || installationUUID.equals(ANY_OTHER_UUID);
        }).count();
        assertEquals(2, apiKeyRepository.getTodayAllowedUUIDCount(apiKey));
        assertEquals(2, numberOfAllowedUUIDs);
    }

    @Test
    public void removesTheAllowedUUIDsCreatedYesterday() {
        ApiKey apiKey = givenAnApiKey();
        givenTodayIsToday();
        apiKeyRepository.addAllowedUUID(apiKey, ANY_UUID);
        apiKeyRepository.addAllowedUUID(apiKey, ANY_OTHER_UUID);

        givenTodayIsTomorrow();
        apiKeyRepository.deleteOldAllowedUUIDs();

        Set<AllowedUUID> allowedUUIDs = apiKeyRepository.getTodayAllowedUUIDS(apiKey);
        long numberOfAllowedUUIDs = allowedUUIDs.stream().filter(allowedUUID -> {
            String installationUUID = allowedUUID.getInstallationUUID();
            return installationUUID.equals(ANY_UUID) || installationUUID.equals(ANY_OTHER_UUID);
        }).count();
        assertEquals(0, apiKeyRepository.getTodayAllowedUUIDCount(apiKey));
        assertEquals(0, numberOfAllowedUUIDs);
    }

    @Test
    public void doesNotCrashIfTheApiKeyDoesNotExist() {
        givenTodayIsToday();

        apiKeyRepository.deleteOldAllowedUUIDs();
    }

    @Test
    public void doesNotCrashIfThereAreNoAllowedUUIDs() {
        givenTodayIsToday();
        ApiKey apiKey = givenAnApiKey();

        apiKeyRepository.deleteOldAllowedUUIDs();

        assertEquals(0, apiKeyRepository.getTodayAllowedUUIDCount(apiKey));
    }

    private void givenTodayIsYesterday() {
        when(time.getTodayNumericDay()).thenReturn(defaultTime.now().minusDays(1).getDayOfMonth());
        when(time.now()).thenReturn(defaultTime.now().minusDays(1));
        when(time.getYesterdayMidnightDate()).thenReturn(defaultTime.getYesterdayMidnightDate().minusDays(1));
        when(time.getTodayMidnightDate()).thenReturn(defaultTime.getTodayMidnightDate().minusDays(1));
        when(time.getTomorrowMidnightDate()).thenReturn(defaultTime.getTomorrowMidnightDate().minusDays(1));
    }

    private void givenTodayIsTomorrow() {
        when(time.getTodayNumericDay()).thenReturn(defaultTime.now().plusDays(1).getDayOfMonth());
        when(time.now()).thenReturn(defaultTime.now().plusDays(1));
        when(time.getYesterdayMidnightDate()).thenReturn(defaultTime.getYesterdayMidnightDate().plusDays(1));
        when(time.getTodayMidnightDate()).thenReturn(defaultTime.getTodayMidnightDate().plusDays(1));
        when(time.getTomorrowMidnightDate()).thenReturn(defaultTime.getTomorrowMidnightDate().plusDays(1));
    }

    private void givenTodayIsToday() {
        when(time.getTodayNumericDay()).thenReturn(defaultTime.getTodayNumericDay());
        when(time.now()).thenReturn(defaultTime.now());
        when(time.getYesterdayMidnightDate()).thenReturn(defaultTime.getYesterdayMidnightDate());
        when(time.getTodayMidnightDate()).thenReturn(defaultTime.getTodayMidnightDate());
        when(time.getTomorrowMidnightDate()).thenReturn(defaultTime.getTomorrowMidnightDate());
    }

    private ApiKey givenAnApiKey() {
        return givenAnApiKey(ANY_API_KEY);
    }

    private ApiKey givenAnApiKey(String apiKeyValue) {
        ApiKey apiKey = apiKeyRepository.create(apiKeyValue);
        apiKeyId = apiKey.getId();
        return apiKey;
    }
}
