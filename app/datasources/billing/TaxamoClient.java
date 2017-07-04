package datasources.billing;

import com.google.inject.name.Named;
import com.taxamo.client.api.TaxamoApi;
import com.taxamo.client.common.ApiException;
import com.taxamo.client.model.*;
import play.Configuration;
import play.Logger;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TaxamoClient {

    private final TaxamoApi api;

    @Inject
    public TaxamoClient(@Named("taxamo") Configuration configuration) {
        this.api = new TaxamoApi(configuration.getString("private_api_key"));
    }

    public ListTransactionsOut getAllTransactions(String customKey) throws ApiException {
        return api.listTransactions(null, null, customKey, null, null, null, null, null, null, null, "0", null, null, null, null);
    }

    public String createPlaceholderTransaction(String country, String plan, String buyerIp) {
        CreateTransactionIn createTransactionIn = new CreateTransactionIn();

        List<InputTransactionLine> lines = new ArrayList<>();
        InputTransactionLine line = new InputTransactionLine();
        line.setCustomId(plan);
        line.setAmount(BigDecimal.ZERO);
        lines.add(line);

        List<CustomFields> fields = new ArrayList<>();
        CustomFields field = new CustomFields();
        field.setKey("placeholder-transaction");
        field.setValue("stripe");
        fields.add(field);

        InputTransaction transaction = new InputTransaction();
        transaction.setTransactionLines(lines);
        transaction.setCustomFields(fields);
        transaction.setCurrencyCode("USD");
        transaction.setBuyerIp(buyerIp);
        transaction.setBillingCountryCode(country);
        transaction.setForceCountryCode(country);
        createTransactionIn.setTransaction(transaction);
        try {
            Logger.debug("Creating transaction: " + transaction);
            CreateTransactionOut transactionOut = api.createTransaction(createTransactionIn);
            Logger.debug("Transaction created: " + transactionOut);
            Logger.debug("Transaction key: " + transactionOut.getTransaction().getKey());

            ConfirmTransactionIn confirmTransactionIn = new ConfirmTransactionIn();
            ConfirmTransactionOut confirmTransactionOut = api.confirmTransaction(transactionOut.getTransaction().getKey(), confirmTransactionIn);
            Logger.debug("Transaction confirmed: " + confirmTransactionOut);

            return confirmTransactionOut.getTransaction().getKey();
        } catch (ApiException e) {
            e.printStackTrace();
        }

        return "";
    }
}
