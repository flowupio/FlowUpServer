package datasources.database;

import models.ApiKey;
import models.Organization;

import java.util.UUID;

public class ApiKeyDatasource {
    public boolean isValuePresentInDB(String apiKeyValue) {
        return ApiKey.find.where().eq("value", apiKeyValue).findRowCount() > 0;
    }

    public ApiKey findByApiKeyValue(String apiKeyValue) {
        return ApiKey.find.where().eq("value", apiKeyValue).findUnique();
    }

    public ApiKey create() {
        String value = UUID.randomUUID().toString().replaceAll("-", "");
        return create(value);
    }

    public ApiKey create(String value) {
        ApiKey apiKey = new ApiKey();
        apiKey.setValue(value);
        apiKey.save();
        return apiKey;
    }
}
