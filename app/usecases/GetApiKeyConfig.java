package usecases;

import models.ApiKey;
import usecases.models.ApiKeyConfig;
import usecases.repositories.ApiKeyRepository;

import javax.inject.Inject;

public class GetApiKeyConfig {

    private final ApiKeyRepository repository;

    @Inject
    public GetApiKeyConfig(ApiKeyRepository repository) {
        this.repository = repository;
    }

    public ApiKeyConfig execute(String apiKeyValue) {
        ApiKey apiKey = repository.getApiKey(apiKeyValue);
        boolean isApiKeyEnabled = apiKey != null && apiKey.isEnabled();
        return new ApiKeyConfig(isApiKeyEnabled);
    }
}
