package usecases;

import models.Version;
import sampling.ApiKeyPrivilege;
import usecases.models.ApiKeyConfig;
import usecases.repositories.ApiKeyRepository;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class GetApiKeyConfig {

    private final ApiKeyRepository repository;
    private final ApiKeyPrivilege apiKeyPrivilege;

    @Inject
    public GetApiKeyConfig(ApiKeyRepository repository, ApiKeyPrivilege apiKeyPrivilege) {
        this.repository = repository;
        this.apiKeyPrivilege = apiKeyPrivilege;
    }

    public CompletionStage<ApiKeyConfig> execute(String apiKeyValue, String uuid, Version version) {
        return repository.getApiKeyAsync(apiKeyValue).thenApply(apiKey -> {
            boolean isApiKeyEnabled = apiKey != null &&
                    apiKey.isEnabled() &&
                    apiKeyPrivilege.isAllowed(apiKeyValue, uuid, version);

            return new ApiKeyConfig(isApiKeyEnabled);
        });
    }
}
