package sampling;

import models.AllowedUUID;
import models.ApiKey;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import repositories.ApiKeyRepository;

import java.util.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
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
    public void returnsIsNotInTheSamplingGroupIfItIsFullAndDoesNotContainsTheUUID() {
        ApiKey apiKey = givenAnApiKeyFullOfUsers(ANY_API_KEY);
        givenTheUUIDIsNotInTheGroup(apiKey, ANY_UUID);

        boolean isUUIDIn = samplingGroup.isIn(ANY_API_KEY, ANY_UUID);

        assertFalse(isUUIDIn);
    }

    @Test
    public void returnsIsInTheSamplingGroupIfItIsFullAndContainsTheUUID() {
        ApiKey apiKey = givenAnApiKeyFullOfUsers(ANY_API_KEY);
        givenTheUUIDIsInTheGroup(apiKey, ANY_UUID);

        boolean isUUIDIn = samplingGroup.isIn(ANY_API_KEY, ANY_UUID);

        assertTrue(isUUIDIn);
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
        return givenAnApiKey(true, anyApiKey, Collections.emptySet());
    }

    private ApiKey givenADisabledApiKey(String anyApiKey) {
        return givenAnApiKey(false, anyApiKey, Collections.emptySet());
    }

    private ApiKey givenAnApiKeyWithAllowedUUIDs(String apiKey, String uuid) {
        AllowedUUID allowedUUID = new AllowedUUID();
        allowedUUID.setInstallationUUID(uuid);
        Set<AllowedUUID> allowedUuids = new HashSet();
        allowedUuids.add(allowedUUID);
        return givenAnApiKey(true, apiKey, allowedUuids);
    }

    private ApiKey givenAnApiKeyFullOfUsers(String apiKeyValue) {
        Set<AllowedUUID> allowedUuids = new HashSet<>();
        UUID uuidId = UUID.randomUUID();
        for (int i = 0; i < ANY_NUMBER_OF_ALLOWED_UUID; i++) {
            AllowedUUID allowedUUID = new AllowedUUID();
            allowedUUID.setId(uuidId);
            allowedUUID.setInstallationUUID(String.valueOf(i));
            allowedUuids.add(allowedUUID);
        }
        when(apiKeyRepository.getTodayAllowedUUIDCount(any(ApiKey.class))).thenReturn(ANY_NUMBER_OF_ALLOWED_UUID);
        return givenAnApiKey(true, apiKeyValue, allowedUuids);
    }

    private ApiKey givenAnApiKey(boolean enabled, String apiKeyValue, Set<AllowedUUID> allowedUuids) {
        ApiKey apiKey = new ApiKey();
        apiKey.setEnabled(enabled);
        apiKey.setNumberOfAllowedUUIDs(ANY_NUMBER_OF_ALLOWED_UUID);
        when(apiKeyRepository.getApiKey(apiKeyValue)).thenReturn(apiKey);
        when(apiKeyRepository.getTodayAllowedUUIDS(apiKey)).thenReturn(allowedUuids);
        return apiKey;
    }

    private void givenTheUUIDIsNotInTheGroup(ApiKey apiKey, String uuid) {
        when(apiKeyRepository.containsAllowedUUID(apiKey, uuid)).thenReturn(false);
    }

    private void givenTheUUIDIsInTheGroup(ApiKey apiKey, String uuid) {
        when(apiKeyRepository.containsAllowedUUID(apiKey, uuid)).thenReturn(true);
    }
}
