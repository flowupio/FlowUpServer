package installationscounter.domain;

import usecases.repositories.ApiKeyRepository;

import javax.inject.Inject;
import java.util.Objects;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class InstallationsCounter {

    private final ApiKeyRepository apiKeyRepository;
    private final InstallationsCounterRepository installationsRepository;

    @Inject
    public InstallationsCounter(ApiKeyRepository apiKeyRepository, InstallationsCounterRepository installationsRepository) {
        this.apiKeyRepository = apiKeyRepository;
        this.installationsRepository = installationsRepository;
    }

    public CompletionStage<Installation> increment(Installation installation) {
        return existApiKey(installation.getApiKey()).thenCompose(exist -> {
            if (exist) {
                return completedFuture(installation);
            }
            return installationsRepository.increment(installation);
        });
    }

    public CompletionStage<Long> getInstallationsCounter(String apiKeyValue) {
        return existApiKey(apiKeyValue).thenCompose(exist -> {
            if (exist) {
                return installationsRepository.getInstallationCounter(apiKeyValue);
            }
            return completedFuture(0L);
        });
    }

    private CompletionStage<Boolean> existApiKey(String apiKeyValue) {
        return apiKeyRepository.getApiKeyAsync(apiKeyValue).thenApply(Objects::nonNull);
    }
}
