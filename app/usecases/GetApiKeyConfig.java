package usecases;

import models.Version;
import sampling.SamplingGroup;
import usecases.models.ApiKeyConfig;
import usecases.repositories.ApiKeyRepository;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class GetApiKeyConfig {

    private final ApiKeyRepository repository;
    private final SamplingGroup samplingGroup;

    @Inject
    public GetApiKeyConfig(ApiKeyRepository repository, SamplingGroup samplingGroup) {
        this.repository = repository;
        this.samplingGroup = samplingGroup;
    }

    public CompletionStage<ApiKeyConfig> execute(String apiKeyValue, String uuid, Version version) {
        return repository.getApiKeyAsync(apiKeyValue).thenApply(apiKey -> {
            boolean isApiKeyEnabled = apiKey != null &&
                    apiKey.isEnabled() &&
                    samplingGroup.isIn(apiKeyValue, uuid, version);

            return new ApiKeyConfig(isApiKeyEnabled);
        });
    }
}
