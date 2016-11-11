package usecases;

import usecases.repositories.ApiKeyRepository;

import javax.inject.Inject;

public class DeleteYesterdayAllowedUUIDs {

    private final ApiKeyRepository apiKeyRepository;

    @Inject
    public DeleteYesterdayAllowedUUIDs(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    public void execute() {
        apiKeyRepository.deleteOldAllowedUUIDs();
    }
}
