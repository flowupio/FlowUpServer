package usecases;

import models.ApiKey;
import sampling.SamplingGroup;
import usecases.models.ApiKeyConfig;
import usecases.repositories.ApiKeyRepository;

import javax.inject.Inject;

public class GetApiKeyConfig {

    private final ApiKeyRepository repository;
    private final SamplingGroup samplingGroup;

    @Inject
    public GetApiKeyConfig(ApiKeyRepository repository, SamplingGroup samplingGroup) {
        this.repository = repository;
        this.samplingGroup = samplingGroup;
    }

    public ApiKeyConfig execute(String apiKeyValue, String uuid) {
        ApiKey apiKey = repository.getApiKey(apiKeyValue);
        boolean isApiKeyEnabled = apiKey != null && apiKey.isEnabled() && samplingGroup.isIn(apiKeyValue, uuid);

        return new ApiKeyConfig(isApiKeyEnabled);
    }
}
