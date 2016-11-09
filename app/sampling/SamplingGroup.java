package sampling;

import models.ApiKey;
import repositories.ApiKeyRepository;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

public class SamplingGroup {

    private final ApiKeyRepository apiKeyRepository;

    @Inject
    public SamplingGroup(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    public boolean isIn(String apiKeyValue, String uuid) {
        ApiKey apiKey = apiKeyRepository.getApiKey(apiKeyValue);
        if(apiKey == null) {
            return false;
        }
        //The Android SDK 0.1.3 or younger versions does not
        // send the UUID as part of the header. This is
        //because the sampling was implemented in client side.
        if (uuid == null) {
            return true;
        }
        return isInSamplingGroup(apiKey);
    }

    private boolean isInSamplingGroup(ApiKey apiKey) {
        if (!apiKey.isEnabled()) {
            return false;
        }
        if (hasExceededTheNumberOfAllowedUUIDs(apiKey)) {
            return false;
        } else {
            //TODO: Insert allowed uuid.
            return true;
        }
    }

    private boolean hasExceededTheNumberOfAllowedUUIDs(ApiKey apiKey) {
        List<UUID> allowedUUIDs = apiKey.getAllowedUUIDs();
        return allowedUUIDs.size() >= apiKey.getNumberOfAllowedUUIDs();
    }
}
