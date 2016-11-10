package datasources.database;

import models.AllowedUUID;
import models.ApiKey;

import java.util.UUID;

public class ApiKeyDatasource {

    public ApiKey findByApiKeyValue(String apiKeyValue) {
        return ApiKey.find.fetch("organization").where().eq("value", apiKeyValue).findUnique();
    }

    public ApiKey create() {
        String value = UUID.randomUUID().toString().replaceAll("-", "");
        return create(value);
    }

    public ApiKey create(String value) {
        return create(value, true);
    }

    public ApiKey create(String value, boolean enabled) {
        ApiKey apiKey = new ApiKey();
        apiKey.setValue(value);
        apiKey.setEnabled(enabled);
        apiKey.save();
        return apiKey;
    }

    public boolean delete(String apiKeyValue) {
        ApiKey apiKey = findByApiKeyValue(apiKeyValue);
        return apiKey != null && apiKey.delete();
    }

    public void addAllowedUUID(ApiKey apiKey, String uuid) {
        AllowedUUID allowedUUID = new AllowedUUID();
        allowedUUID.setInstallationUUID(uuid);
        allowedUUID.setApiKey(apiKey);
        allowedUUID.save();
    }
}
