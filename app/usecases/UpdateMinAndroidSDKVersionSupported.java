package usecases;

import com.google.inject.Inject;
import models.ApiKey;
import models.Platform;
import models.Version;
import usecases.repositories.ApiKeyRepository;

import java.util.UUID;

public class UpdateMinAndroidSDKVersionSupported {

    private final ApiKeyRepository repository;

    @Inject
    public UpdateMinAndroidSDKVersionSupported(ApiKeyRepository repository) {
        this.repository = repository;
    }

    public ApiKey execute(String apiKeyId, String plainVersion) {
        Version version = Version.fromString(plainVersion);
        if (!isVersionValid(version) || !isApiKeyIdValid(apiKeyId)) {
            return null;
        }
        return repository.updateMinAndroidSdkSupported(UUID.fromString(apiKeyId), version);
    }

    private boolean isVersionValid(Version version) {
        return version != Version.UNKNOWN_VERSION && version.getPlatform() == Platform.ANDROID;
    }

    private boolean isApiKeyIdValid(String id) {
        return id != null;
    }
}
