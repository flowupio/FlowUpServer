package installationscounter.usecase;

import installationscounter.domain.InstallationsCounter;
import installationscounter.domain.InstallationsCounterRepository;
import models.ApiKey;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import usecases.repositories.ApiKeyRepository;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GetInstallationsCounterByApiKeyTest {

    private static final String ANY_API_KEY = "bec94b0431354ba0bcf0b996960cd262";
    private static final long ANY_NUMBER_OF_INSTALLATIONS = 11;

    private GetInstallationsCounterByApiKey getInstallations;
    @Mock
    private InstallationsCounterRepository installationsCounterRepository;
    @Mock
    private ApiKeyRepository apiKeyRepository;

    @Before
    public void setUp() {
        InstallationsCounter installationsCounter = new InstallationsCounter(apiKeyRepository, installationsCounterRepository);
        getInstallations = new GetInstallationsCounterByApiKey(installationsCounter);
    }

    @Test
    public void returnsZeroIfTheApiKeyDoesNotExist() throws Exception {
        givenThereIsNoApiKey(ANY_API_KEY);

        long installations = getInstallations.execute(ANY_API_KEY).toCompletableFuture().get();

        assertEquals(0, installations);
    }

    @Test
    public void returnsTheNumberOfInstallationsIfTheApiKeyExists() throws Exception {
        givenThereIsAnApiKeyWithInstallations(ANY_API_KEY, ANY_NUMBER_OF_INSTALLATIONS);

        long installations = getInstallations.execute(ANY_API_KEY).toCompletableFuture().get();

        assertEquals(ANY_NUMBER_OF_INSTALLATIONS, installations);
    }

    private void givenThereIsAnApiKeyWithInstallations(String apiKeyValue, long anyNumberOfInstallations) {
        ApiKey value = new ApiKey();
        value.setValue(apiKeyValue);
        when(apiKeyRepository.getApiKeyAsync(apiKeyValue)).thenReturn(completedFuture(value));
        when(installationsCounterRepository.getInstallationCounter(apiKeyValue)).thenReturn(completedFuture(anyNumberOfInstallations));
    }

    private void givenThereIsNoApiKey(String apiKey) {
        when(apiKeyRepository.getApiKeyAsync(apiKey)).thenReturn(completedFuture(null));
    }

}