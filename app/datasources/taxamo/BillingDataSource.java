package datasources.taxamo;

import com.taxamo.client.common.ApiException;
import com.taxamo.client.model.ListTransactionsOut;
import models.Transaction;
import play.Logger;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BillingDataSource {

    private final TaxamoClient client;
    private final TransactionsMapper mapper;

    @Inject
    public BillingDataSource(TaxamoClient client, TransactionsMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    public List<Transaction> getTransactions(String billingId) {
        List<Transaction> transactions = new ArrayList<>();

        try {
            ListTransactionsOut transactionsResult = client.getAllTransactions(billingId);
            if (transactionsResult != null) {
                transactions.addAll(transactionsResult.getTransactions()
                        .stream()
                        .map(mapper::mapTransaction)
                        .collect(Collectors.toList())
                );
            }
        } catch (ApiException e) {
            Logger.error("Failed retrieving transactions for billing id: " + billingId, e);
        }

        return transactions;
    }
}
