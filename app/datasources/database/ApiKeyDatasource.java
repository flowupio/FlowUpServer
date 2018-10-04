package datasources.database;

import com.avaje.ebean.ExpressionList;
import models.AllowedUUID;
import models.ApiKey;
import models.Version;
import org.joda.time.DateTime;
import utils.Time;

import javax.inject.Inject;
import java.util.Set;
import java.util.UUID;

public class ApiKeyDatasource {

    private final Time time;

    @Inject
    public ApiKeyDatasource(Time time) {
        this.time = time;
    }

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
        return create(value, enabled, null);
    }

    public ApiKey create(String value, boolean enabled, Version minAndroidSdkSupported) {
        ApiKey apiKey = new ApiKey();
        if (minAndroidSdkSupported != null) {
            apiKey.setMinAndroidSDKSupported(minAndroidSdkSupported.toString());
        }
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

    public int getThisMonthAllowedUUIDsCount(ApiKey apiKey) {
        return getThisMonthAllowedUUIDQuery(apiKey).findRowCount();
    }

    public Set<AllowedUUID> getThisMonthAllowedUUIDs(ApiKey apiKey) {
        return getThisMonthAllowedUUIDQuery(apiKey).findSet();
    }

    public void deleteAllowedUUIDs() {
        DateTime today = time.getMonthAgoMidnightDate();
        AllowedUUID.find.where().le("created_at", today).delete();
    }

    private ExpressionList<AllowedUUID> getThisMonthAllowedUUIDQuery(ApiKey apiKey) {
        DateTime monthAgo = time.getMonthAgoMidnightDate();
        DateTime tomorrow = time.getTomorrowMidnightDate();
        return AllowedUUID.find.where().eq("api_key_id", apiKey.getId()).between("created_at", monthAgo, tomorrow);
    }
}
