package datasources.billing;

import com.taxamo.client.common.ApiException;
import com.taxamo.client.model.ListTransactionsOut;
import play.Logger;
import usecases.models.Billing;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class BillingDataSource {

    private final TaxamoClient taxamo;
    private final StripeClient stripe;
    private final BillingMapper mapper;

    @Inject
    public BillingDataSource(TaxamoClient taxamo, StripeClient stripe, BillingMapper mapper) {
        this.taxamo = taxamo;
        this.stripe = stripe;
        this.mapper = mapper;
    }

    public CompletionStage<Billing> getBilling(String billingId) {
        return CompletableFuture.supplyAsync(() -> getBillingSync(billingId));
    }

    public CompletionStage<Boolean> createSubscription(String email, String country, String buyerIp, String token, String plan, int quantity) {
        return CompletableFuture.supplyAsync(() -> {
            String transactionKey = taxamo.createPlaceholderTransaction(country, plan, buyerIp);
            stripe.createSubscription(email, token, plan, quantity, transactionKey);
            return true;
        });
    }

    @Nullable
    private Billing getBillingSync(String billingId) {
        if (billingId == null) {
            return null;
        }

        Billing billing = null;
        try {
            ListTransactionsOut transactionsResult = taxamo.getAllTransactions(billingId);
            if (transactionsResult != null) {
                billing = mapper.mapBilling(transactionsResult.getTransactions());
            }
        } catch (ApiException e) {
            Logger.error("Failed connecting to Taxamo API for billing id: " + billingId, e);
        }

        return billing;
    }
}
