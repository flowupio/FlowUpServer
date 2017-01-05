package datasources.taxamo;

import com.google.inject.name.Named;
import com.taxamo.client.api.TaxamoApi;
import com.taxamo.client.common.ApiException;
import com.taxamo.client.model.ListTransactionsOut;
import play.Configuration;

import javax.inject.Inject;

public class TaxamoClient {

    private final TaxamoApi api;

    @Inject
    public TaxamoClient(@Named("taxamo") Configuration configuration) {
        this.api = new TaxamoApi(configuration.getString("private_api_key"));
    }

    public ListTransactionsOut getAllTransactions(String customKey) throws ApiException {
        return api.listTransactions(null, null, customKey, null, null, null, null, null, null, null, "0", null, null, null, null);
    }
}
