package repositories;

import models.ApiKey;
import org.junit.Before;
import org.junit.Test;
import play.cache.CacheApi;
import play.inject.Injector;
import utils.WithFlowUpApplication;

import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class ApiKeyRepositoryTests extends WithFlowUpApplication {

    private static final String ANY_UUID = "abcd";
    private static final java.lang.String ANY_API_KEY = "12345";

    private ApiKeyRepository apiKeyRepository;
    private CacheApi cache;

    @Before
    @Override
    public void startPlay() {
        super.startPlay();
        Injector injector = app.injector();
        apiKeyRepository = injector.instanceOf(ApiKeyRepository.class);
        cache = spy(injector.instanceOf(CacheApi.class));
    }

    @Test
    public void addsTheNewAllowedUUIDToTheApiKey() {
        ApiKey originalApiKey = givenAnApiKey();

        apiKeyRepository.addAllowedUUID(originalApiKey, ANY_UUID);
        ApiKey apiKey = apiKeyRepository.getApiKey(originalApiKey.getValue());

        assertTrue(apiKeyRepository.containsAllowedUUID(apiKey, ANY_UUID));
    }

    @Test
    public void flushesTheCachedApiKeyAllowedUUIDCounterWithTheNewValue() {
        ApiKey originalApiKey = givenAnApiKey();

        apiKeyRepository.addAllowedUUID(originalApiKey, ANY_UUID);
        ApiKey apiKey = apiKeyRepository.getApiKey(originalApiKey.getValue());

        verify(cache).remove("apiKey.todayAllowedUUIDCount." + apiKey.getId());
    }



    @Test
    public void flushesTheAllowedUUIDsWithTheNewValue() {
        ApiKey originalApiKey = givenAnApiKey();

        apiKeyRepository.addAllowedUUID(originalApiKey, ANY_UUID);
        ApiKey apiKey = apiKeyRepository.getApiKey(originalApiKey.getValue());

        verify(cache).remove("allowedUUIDs." + apiKey.getId());
    }

    //Flush allowed uuid cache

    private ApiKey givenAnApiKey() {
        return apiKeyRepository.create(ANY_API_KEY);
    }
}
