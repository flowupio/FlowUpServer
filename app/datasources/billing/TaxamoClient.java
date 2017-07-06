package datasources.billing;

import com.google.inject.name.Named;
import com.taxamo.client.api.TaxamoApi;
import com.taxamo.client.common.ApiException;
import com.taxamo.client.model.*;
import models.CreateSubscriptionRequest;
import play.Configuration;
import play.Logger;

import javax.inject.Inject;
import java.util.Optional;

public class TaxamoClient {

    public static final String CARD_NUMBER_SUFFIX_KEY = "card-suffix";
    public static final String PLAN_ID_KEY = "stripe-plan-id";

    private final TaxamoApi api;
    private final TaxamoTransactionMapper mapper;

    @Inject
    public TaxamoClient(@Named("taxamo") Configuration configuration, TaxamoTransactionMapper mapper) {
        this.api = new TaxamoApi(configuration.getString("private_api_key"));
        this.mapper = mapper;
    }

    public ListTransactionsOut getAllTransactions(String customKey) throws ApiException {
        return api.listTransactions(null, null, customKey, null, null, null, null, null, null, null, "0", null, null, null, null);
    }

    public Optional<String> createPlaceholderTransaction(CreateSubscriptionRequest createSubscriptionRequest, String billingId) {
        try {
            CreateTransactionIn createTransactionIn = mapper.map(createSubscriptionRequest, billingId);
            CreateTransactionOut transactionOut = api.createTransaction(createTransactionIn);

            ConfirmTransactionIn confirmTransactionIn = new ConfirmTransactionIn();
            ConfirmTransactionOut confirmTransactionOut = api.confirmTransaction(transactionOut.getTransaction().getKey(), confirmTransactionIn);

            return Optional.of(confirmTransactionOut.getTransaction().getKey());
        } catch (ApiException e) {
            Logger.error("Error creating placeholder transaction in taxamo: " + createSubscriptionRequest + " for billing with id: " + billingId, e);
            return Optional.empty();
        }
    }
}
