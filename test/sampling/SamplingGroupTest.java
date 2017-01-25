package sampling;

import models.AllowedUUID;
import models.ApiKey;
import models.Platform;
import models.Version;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import usecases.repositories.ApiKeyRepository;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SamplingGroupTest {

    private static final Version ANY_VERSION = new Version(0, 1, 2, Platform.ANDROID);

    private static final String ANY_API_KEY = "12345";
    private static final String ANY_UUID = "abcd";
    private static final int ANY_NUMBER_OF_ALLOWED_UUID = 50;
    private static final Version VERSION_ONE = new Version(0, 0, 1, Platform.ANDROID);
    private static final Version VERSION_TWO = new Version(0, 2, 1, Platform.ANDROID);

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

        boolean isUUIDIn = samplingGroup.isIn(ANY_API_KEY, ANY_UUID, ANY_VERSION);

        assertFalse(isUUIDIn);
    }

    @Test
    public void returnsIsInTheSamplingGroupIfItIsFullAndContainsTheUUID() {
        ApiKey apiKey = givenAnApiKeyFullOfUsers(ANY_API_KEY);
        givenTheUUIDIsInTheGroup(apiKey, ANY_UUID);

        boolean isUUIDIn = samplingGroup.isIn(ANY_API_KEY, ANY_UUID, ANY_VERSION);

        assertTrue(isUUIDIn);
    }

    @Test
    public void returnsIsNotInTheSamplingGroupIfTheApiKeyIsDisabled() {
        givenADisabledApiKey(ANY_API_KEY);

        boolean isUUIDIn = samplingGroup.isIn(ANY_API_KEY, ANY_UUID, ANY_VERSION);

        assertFalse(isUUIDIn);
    }

    @Test
    public void returnsIsInTheSamplingGroupIfTheApiKeyHasTheUUIDAsPartOfTheAllowedUUIDList() {
        givenAnApiKeyWithAllowedUUIDs(ANY_API_KEY, ANY_UUID);

        boolean isUUIDIn = samplingGroup.isIn(ANY_API_KEY, ANY_UUID, ANY_VERSION);

        assertTrue(isUUIDIn);
    }

    @Test
    public void returnsIsInTheSamplingGroupIfIsANewUUIDButTheGroupIsNotFull() {
        givenAnApiKeyWithSpaceInTheSamplingGroup(ANY_API_KEY);

        boolean isUUIDIn = samplingGroup.isIn(ANY_API_KEY, ANY_UUID, ANY_VERSION);

        assertTrue(isUUIDIn);
    }

    @Test
    public void updatesTheApiKeyWithTheNewUUIDIfTheSamplingGroupIsNotFull() {
        ApiKey apiKey = givenAnApiKeyWithSpaceInTheSamplingGroup(ANY_API_KEY);

        samplingGroup.isIn(ANY_API_KEY, ANY_UUID, ANY_VERSION);

        verify(apiKeyRepository).addAllowedUUID(apiKey, ANY_UUID);
    }

    @Test
    public void apiKeyDefaultVersionShouldBeTheInitialOne() {
        ApiKey apiKey = new ApiKey();
        Version initialVersion = new Version(0, 0, 0, Platform.ANDROID);

        assertEquals(initialVersion, Version.fromString(apiKey.getMinAndroidSDKSupported()));
    }

    @Test
    public void nonSupportedVersionsShouldNotBeInTheSamplingGroup() {
        givenAnApiKeyWithMinAndroidVersionSupported(VERSION_TWO);

        assertFalse(samplingGroup.isIn(ANY_API_KEY, ANY_UUID, VERSION_ONE));
    }

    @Test
    public void supportedVersionsShouldBePartOfTheSamplingGroup() {
        givenAnApiKeyWithMinAndroidVersionSupported(VERSION_ONE);

        assertTrue(samplingGroup.isIn(ANY_API_KEY, ANY_UUID, VERSION_TWO));
    }

    @Test
    public void theSameVersionShouldBePartOfTheSamplingGroup() {
        givenAnApiKeyWithMinAndroidVersionSupported(VERSION_ONE);

        assertTrue(samplingGroup.isIn(ANY_API_KEY, ANY_UUID, VERSION_ONE));
    }

    private ApiKey givenAnApiKeyWithMinAndroidVersionSupported(Version version) {
        return givenAnApiKey(true, ANY_API_KEY, new HashSet<>(), version.toString());
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
        Set<AllowedUUID> allowedUuids = new HashSet<>();
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
        return givenAnApiKey(enabled, apiKeyValue, allowedUuids, "FlowUpAndroidSDK/0.0.0");
    }

    private ApiKey givenAnApiKey(boolean enabled, String apiKeyValue, Set<AllowedUUID> allowedUuids, String minAndroidVersion) {
        ApiKey apiKey = new ApiKey();
        apiKey.setEnabled(enabled);
        apiKey.setNumberOfAllowedUUIDs(ANY_NUMBER_OF_ALLOWED_UUID);
        apiKey.setMinAndroidSDKSupported(minAndroidVersion);
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
