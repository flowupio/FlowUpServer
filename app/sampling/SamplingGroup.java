package sampling;

import models.AllowedUUID;
import models.ApiKey;
import repositories.ApiKeyRepository;

import javax.inject.Inject;
import java.util.List;

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
        return isInSamplingGroup(apiKey, uuid);
    }

    private boolean isInSamplingGroup(ApiKey apiKey, String uuid) {
        if (!apiKey.isEnabled()) {
            return false;
        }
        if (hasExceededTheNumberOfAllowedUUIDs(apiKey)) {
            return apiKey.containsAllowedUUID(uuid);
        } else {
            apiKeyRepository.addAllowedUUID(apiKey, uuid);
            return true;
        }
    }

    private boolean hasExceededTheNumberOfAllowedUUIDs(ApiKey apiKey) {
        List<AllowedUUID> allowedUUIDs = apiKey.getAllowedUUIDs();
        return allowedUUIDs.size() >= apiKey.getNumberOfAllowedUUIDs();
    }
}
