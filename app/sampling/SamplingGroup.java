package sampling;

import models.ApiKey;
import usecases.repositories.ApiKeyRepository;

import javax.inject.Inject;

public class SamplingGroup {

    private final ApiKeyRepository apiKeyRepository;

    @Inject
    public SamplingGroup(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    public boolean isIn(String apiKeyValue, String uuid) {
        ApiKey apiKey = apiKeyRepository.getApiKey(apiKeyValue);
        if (apiKey == null) {
            return false;
        }
        //The Android SDK 0.1.3 or younger versions does not
        // send the UUID as part of the header. This is
        //because the sampling was implemented in client side.
        if (uuid == null) {
            return true;
        }
        return isInSamplingGroup(apiKey, uuid);
    }

    private boolean isInSamplingGroup(ApiKey apiKey, String uuid) {
        if (!apiKey.isEnabled()) {
            return false;
        }
        if (hasExceededTheNumberOfAllowedUUIDs(apiKey)) {
            return apiKeyRepository.containsAllowedUUID(apiKey, uuid);
        } else {
            apiKeyRepository.addAllowedUUID(apiKey, uuid);
            return true;
        }
    }

    private boolean hasExceededTheNumberOfAllowedUUIDs(ApiKey apiKey) {
        int allowedUUIDCount = apiKeyRepository.getTodayAllowedUUIDCount(apiKey);
        return allowedUUIDCount >= apiKey.getNumberOfAllowedUUIDs();
    }
}
