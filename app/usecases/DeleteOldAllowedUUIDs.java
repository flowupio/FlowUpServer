package usecases;

import usecases.repositories.ApiKeyRepository;

import javax.inject.Inject;

public class DeleteOldAllowedUUIDs {

    private final ApiKeyRepository apiKeyRepository;

    @Inject
    public DeleteOldAllowedUUIDs(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    public void execute(String apiKey) {
        apiKeyRepository.deleteOldAllowedUUIDs(apiKey);
    }
}
