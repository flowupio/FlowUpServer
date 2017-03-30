package installationscounter.usecase;

import installationscounter.domain.Installation;
import installationscounter.domain.InstallationsCounter;
import installationscounter.domain.InstallationsCounterRepository;
import models.ApiKey;
import models.Version;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import usecases.repositories.ApiKeyRepository;
import utils.Time;

import java.util.UUID;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IncrementsInstallationsCounterTest {

    private static final String ANY_API_KEY = "bec94b0431354ba0bcf0b996960cd262";
    private static final String ANY_UUID = UUID.randomUUID().toString();
    private static final Version ANY_DEBUG_VERSION = Version.fromString("FlowUpAndroidSDK/0.3.0-DEBUG");
    private static final Version ANY_VERSION = Version.fromString("FlowUpAndroidSDK/0.3.0");
    private static final Installation ANY_DEBUG_INSTALLATION = new Installation(ANY_API_KEY, ANY_UUID, ANY_DEBUG_VERSION, new Time().now().getMillis());
    private static final Installation ANY_INSTALLATION = new Installation(ANY_API_KEY, ANY_UUID, ANY_VERSION, new Time().now().getMillis());

    private IncrementInstallationsCounter incrementInstallationsCounter;

    @Mock
    private InstallationsCounterRepository installationsCounterRepository;
    @Mock
    private ApiKeyRepository apiKeyRepository;

    @Before
    public void setUp() {
        InstallationsCounter installationsCounter = new InstallationsCounter(apiKeyRepository, installationsCounterRepository);
        incrementInstallationsCounter = new IncrementInstallationsCounter(installationsCounter);
    }

    @Test
    public void doesNotIncrementCounterIfTheInstallationHasDebugEnabled() throws Exception {
        givenThereIsAnApiKey(ANY_API_KEY);

        Installation installation = incrementCounter(ANY_DEBUG_INSTALLATION);

        assertEquals(ANY_DEBUG_INSTALLATION, installation);
        verify(installationsCounterRepository, never()).increment(any());
    }

    @Test
    public void doesNotIncrementCounterIfTheInstallationIsValidButThereIsNoAnApiKeyAssociated() throws Exception {
        givenThereIsNoApiKey(ANY_API_KEY);

        Installation installation = incrementCounter(ANY_INSTALLATION);

        assertEquals(ANY_INSTALLATION, installation);
        verify(installationsCounterRepository, never()).increment(any());
    }

    @Test
    public void incrementsTheCounterIfTheInstallationIsValidAndThereIsAnApiKeyAssociated() throws Exception {
        givenThereIsAnApiKey(ANY_API_KEY);
        givenTheInstallationsCounterCanBeIncremented(ANY_INSTALLATION);

        Installation installation = incrementCounter(ANY_INSTALLATION);

        assertEquals(ANY_INSTALLATION, installation);
        verify(installationsCounterRepository).increment(ANY_INSTALLATION);
    }

    private void givenTheInstallationsCounterCanBeIncremented(Installation installation) {
        when(installationsCounterRepository.increment(installation)).thenReturn(completedFuture(installation));
    }

    private Installation incrementCounter(Installation installation) throws InterruptedException, java.util.concurrent.ExecutionException {
        return incrementInstallationsCounter.execute(installation).toCompletableFuture().get();
    }

    private void givenThereIsAnApiKey(String apiKeyValue) {
        ApiKey value = new ApiKey();
        value.setValue(apiKeyValue);
        when(apiKeyRepository.getApiKeyAsync(apiKeyValue)).thenReturn(completedFuture(value));
    }

    private void givenThereIsNoApiKey(String apiKey) {
        when(apiKeyRepository.getApiKeyAsync(apiKey)).thenReturn(completedFuture(null));
    }
}
