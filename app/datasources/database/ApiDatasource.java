package datasources.database;

import models.ApiKey;

public class ApiDatasource {
    public boolean isValuePresentInDB(String apiKeyValue) {
        return ApiKey.find.where().eq("value", apiKeyValue).findRowCount() > 0;
    }
}
