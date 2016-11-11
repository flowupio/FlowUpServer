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
    private static final java.lang.String ANY_API_KEY = "12345";

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
        when(time.getTodayMidnightDate()).thenReturn(defaultTime.getTodayMidnightDate());
        when(time.getTomorrowMidhtDate()).thenReturn(defaultTime.getTomorrowMidhtDate());
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

    private void givenTodayIsYesterday() {
        when(time.getTodayMidnightDate()).thenReturn(defaultTime.getYesterdayMidnightDate());
        when(time.getTomorrowMidhtDate()).thenReturn(defaultTime.getTodayMidnightDate());
    }

    private ApiKey givenAnApiKey() {
        ApiKey apiKey = apiKeyRepository.create(ANY_API_KEY);
        apiKeyId = apiKey.getId();
        return apiKey;
    }
}
