package datasources.taxamo;

import com.taxamo.client.common.ApiException;
import com.taxamo.client.model.ListTransactionsOut;
import play.Logger;
import usecases.models.Billing;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class BillingDataSource {

    private final TaxamoClient client;
    private final BillingMapper mapper;

    @Inject
    public BillingDataSource(TaxamoClient client, BillingMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    public CompletionStage<Billing> getBilling(String billingId) {
        return CompletableFuture.supplyAsync(() -> getSyncBilling(billingId));
    }

    @Nullable
    private Billing getSyncBilling(String billingId) {
        if (billingId == null) {
            return null;
        }

        Billing billing = null;
        try {
            ListTransactionsOut transactionsResult = client.getAllTransactions(billingId);
            if (transactionsResult != null) {
                billing = mapper.mapBilling(transactionsResult.getTransactions());
            }
        } catch (ApiException e) {
            Logger.error("Failed connecting to Taxamo API for billing id: " + billingId, e);
        }

        return billing;
    }
}
