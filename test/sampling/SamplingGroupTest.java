package sampling;

import models.AllowedUUID;
import models.ApiKey;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import repositories.ApiKeyRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SamplingGroupTest {

    private static final String ANY_API_KEY = "12345";
    private static final String ANY_UUID = "abcd";
    private static final int ANY_NUMBER_OF_ALLOWED_UUID = 50;

    @Mock
    private ApiKeyRepository apiKeyRepository;
    private SamplingGroup samplingGroup;

    @Before
    public void setUp() {
        samplingGroup = new SamplingGroup(apiKeyRepository);
    }

    @Test
    public void returnsIsNotInTheSamplingGroupIfItIsFull() {
        givenAnApiKeyFullOfUsers(ANY_API_KEY);

        boolean isUUIDIn = samplingGroup.isIn(ANY_API_KEY, ANY_UUID);

        assertFalse(isUUIDIn);
    }

    @Test
    public void returnsIsNotInTheSamplingGroupIfTheApiKeyIsDisabled() {
        givenADisabledApiKey(ANY_API_KEY);

        boolean isUUIDIn = samplingGroup.isIn(ANY_API_KEY, ANY_UUID);

        assertFalse(isUUIDIn);
    }

    @Test
    public void returnsIsInTheSamplingGroupIfTheApiKeyHasTheUUIDAsPartOfTheAllowedUUIDList() {
        givenAnApiKeyWithAllowedUUIDs(ANY_API_KEY, ANY_UUID);

        boolean isUUIDIn = samplingGroup.isIn(ANY_API_KEY, ANY_UUID);

        assertTrue(isUUIDIn);
    }

    @Test
    public void returnsIsInTheSamplingGroupIfIsANewUUIDButTheGroupIsNotFull() {
        givenAnApiKeyWithSpaceInTheSamplingGroup(ANY_API_KEY);

        boolean isUUIDIn = samplingGroup.isIn(ANY_API_KEY, ANY_UUID);

        assertTrue(isUUIDIn);
    }

    @Test
    public void updatesTheApiKeyWithTheNewUUIDIfTheSamplingGroupIsNotFull() {
        ApiKey apiKey = givenAnApiKeyWithSpaceInTheSamplingGroup(ANY_API_KEY);

        samplingGroup.isIn(ANY_API_KEY, ANY_UUID);

        verify(apiKeyRepository).addAllowedUUID(apiKey, ANY_UUID);
    }

    private ApiKey givenAnApiKeyWithSpaceInTheSamplingGroup(String anyApiKey) {
        return givenAnApiKey(true, anyApiKey, Collections.emptyList());
    }

    private ApiKey givenADisabledApiKey(String anyApiKey) {
        return givenAnApiKey(false, anyApiKey, Collections.emptyList());
    }

    private ApiKey givenAnApiKeyWithAllowedUUIDs(String apiKey, String uuid) {
        AllowedUUID allowedUUID = new AllowedUUID(uuid);
        List<AllowedUUID> allowedUuids = Collections.singletonList(allowedUUID);
        return givenAnApiKey(true, apiKey, allowedUuids);
    }

    private ApiKey givenAnApiKeyFullOfUsers(String apiKeyValue) {
        List<AllowedUUID> allowedUuids = new ArrayList<>(ANY_NUMBER_OF_ALLOWED_UUID);
        for (int i = 0; i < ANY_NUMBER_OF_ALLOWED_UUID; i++) {
            AllowedUUID allowedUUID = new AllowedUUID(String.valueOf(i));
            allowedUuids.add(allowedUUID);
        }
        return givenAnApiKey(true, apiKeyValue, allowedUuids);
    }

    private ApiKey givenAnApiKey(boolean enabled, String apiKeyValue, List<AllowedUUID> allowedUuids) {
        ApiKey apiKey = new ApiKey();
        apiKey.setEnabled(enabled);
        apiKey.setNumberOfAllowedUUIDs(ANY_NUMBER_OF_ALLOWED_UUID);
        apiKey.setAllowedUUIDs(allowedUuids);
        when(apiKeyRepository.getApiKey(apiKeyValue)).thenReturn(apiKey);
        return apiKey;
    }
}
