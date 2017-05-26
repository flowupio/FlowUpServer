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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ApiKeyPrivilegeTest {

    private static final Version ANY_VERSION = new Version(0, 1, 2, Platform.ANDROID);
    private static final Version ANY_DEBUG_VERSION = new Version(0, 1, 2, Platform.ANDROID, true);

    private static final String ANY_API_KEY = "12345";
    private static final String ANY_UUID = "abcd";
    private static final int ANY_NUMBER_OF_ALLOWED_UUID = 50;
    private static final Version VERSION_ONE = new Version(0, 0, 1, Platform.ANDROID);
    private static final Version VERSION_ONE_DEBUG = new Version(0, 0, 1, Platform.ANDROID, true);
    private static final Version VERSION_TWO = new Version(0, 2, 1, Platform.ANDROID);
    private static final Version IOS_VERSION = new Version(0, 0, 1, Platform.IOS);


    @Mock
    private ApiKeyRepository apiKeyRepository;
    private ApiKeyPrivilege apiKeyPrivilege;

    @Before
    public void setUp() {
        apiKeyPrivilege = new ApiKeyPrivilege(apiKeyRepository);
    }

    @Test
    public void returnsIsNotInTheSamplingGroupIfItIsFullAndDoesNotContainsTheUUID() {
        ApiKey apiKey = givenAnApiKeyFullOfUsers(ANY_API_KEY);
        givenTheUUIDIsNotInTheGroup(apiKey, ANY_UUID);

        boolean isUUIDIn = apiKeyPrivilege.isAllowed(ANY_API_KEY, ANY_UUID, ANY_VERSION);

        assertFalse(isUUIDIn);
    }

    @Test
    public void returnsIsInTheSamplingGroupIfItIsFullAndContainsTheUUID() {
        ApiKey apiKey = givenAnApiKeyFullOfUsers(ANY_API_KEY);
        givenTheUUIDIsInTheGroup(apiKey, ANY_UUID);

        boolean isUUIDIn = apiKeyPrivilege.isAllowed(ANY_API_KEY, ANY_UUID, ANY_VERSION);

        assertTrue(isUUIDIn);
    }

    @Test
    public void returnsIsNotInTheSamplingGroupIfTheApiKeyIsDisabled() {
        givenADisabledApiKey(ANY_API_KEY);

        boolean isUUIDIn = apiKeyPrivilege.isAllowed(ANY_API_KEY, ANY_UUID, ANY_VERSION);

        assertFalse(isUUIDIn);
    }

    @Test
    public void returnsIsInTheSamplingGroupIfTheApiKeyHasTheUUIDAsPartOfTheAllowedUUIDList() {
        givenAnApiKeyWithAllowedUUIDs(ANY_API_KEY, ANY_UUID);

        boolean isUUIDIn = apiKeyPrivilege.isAllowed(ANY_API_KEY, ANY_UUID, ANY_VERSION);

        assertTrue(isUUIDIn);
    }

    @Test
    public void returnsIsInTheSamplingGroupIfIsANewUUIDButTheGroupIsNotFull() {
        givenAnApiKeyWithSpaceInTheSamplingGroup(ANY_API_KEY);

        boolean isUUIDIn = apiKeyPrivilege.isAllowed(ANY_API_KEY, ANY_UUID, ANY_VERSION);

        assertTrue(isUUIDIn);
    }

    @Test
    public void updatesTheApiKeyWithTheNewUUIDIfTheSamplingGroupIsNotFull() {
        ApiKey apiKey = givenAnApiKeyWithSpaceInTheSamplingGroup(ANY_API_KEY);

        apiKeyPrivilege.isAllowed(ANY_API_KEY, ANY_UUID, ANY_VERSION);

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
        givenAnApiKeyWithMinVersionSupported(VERSION_TWO);

        assertFalse(apiKeyPrivilege.isAllowed(ANY_API_KEY, ANY_UUID, VERSION_ONE));
    }

    @Test
    public void supportedVersionsShouldBePartOfTheSamplingGroup() {
        givenAnApiKeyWithMinVersionSupported(VERSION_ONE);

        assertTrue(apiKeyPrivilege.isAllowed(ANY_API_KEY, ANY_UUID, VERSION_TWO));
    }

    @Test
    public void supportedIOSVersionsShouldBePartOfTheSamplingGroup() {
        givenAnApiKeyWithMinVersionSupported(IOS_VERSION);

        assertTrue(apiKeyPrivilege.isAllowed(ANY_API_KEY, ANY_UUID, VERSION_TWO));
    }

    @Test
    public void theSameVersionShouldBePartOfTheSamplingGroup() {
        givenAnApiKeyWithMinVersionSupported(VERSION_ONE);

        assertTrue(apiKeyPrivilege.isAllowed(ANY_API_KEY, ANY_UUID, VERSION_ONE));
    }

    @Test
    public void debugReportsShouldBeSavedButNotAddedToTheSamplingGroupEvenIfTheSamplingGroupIsFull() {
        givenAnApiKeyFullOfUsers(ANY_API_KEY);

        boolean isUUIDIn = apiKeyPrivilege.isAllowed(ANY_API_KEY, ANY_UUID, ANY_DEBUG_VERSION);

        assertTrue(isUUIDIn);
        verify(apiKeyRepository, never()).addAllowedUUID(any(), any());
    }

    @Test
    public void debugReportsShouldBeSavedButNotAddedToTheSamplingGroupEvenIfTheSamplingGroupIsNotFull() {
        givenAnApiKeyWithSpaceInTheSamplingGroup(ANY_API_KEY);

        boolean isUUIDIn = apiKeyPrivilege.isAllowed(ANY_API_KEY, ANY_UUID, ANY_DEBUG_VERSION);

        assertTrue(isUUIDIn);
        verify(apiKeyRepository, never()).addAllowedUUID(any(), any());
    }

    @Test
    public void doesNotAcceptInvalidVersionsEvenIfTheVersionUsedIsTheDebugOne() {
        givenAnApiKeyWithMinVersionSupported(VERSION_TWO);

        assertFalse(apiKeyPrivilege.isAllowed(ANY_API_KEY, ANY_UUID, VERSION_ONE_DEBUG));
    }

    private ApiKey givenAnApiKeyWithMinVersionSupported(Version version) {
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
        when(apiKeyRepository.getThisMonthAllowedUUIDCount(any(ApiKey.class))).thenReturn(ANY_NUMBER_OF_ALLOWED_UUID);
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
        when(apiKeyRepository.getThisMonthAllowedUUIDS(apiKey)).thenReturn(allowedUuids);
        return apiKey;
    }

    private void givenTheUUIDIsNotInTheGroup(ApiKey apiKey, String uuid) {
        when(apiKeyRepository.containsAllowedUUID(apiKey, uuid)).thenReturn(false);
    }

    private void givenTheUUIDIsInTheGroup(ApiKey apiKey, String uuid) {
        when(apiKeyRepository.containsAllowedUUID(apiKey, uuid)).thenReturn(true);
    }
}
