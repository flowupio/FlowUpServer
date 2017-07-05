package datasources.billing;

import com.google.inject.name.Named;
import com.taxamo.client.api.TaxamoApi;
import com.taxamo.client.common.ApiException;
import com.taxamo.client.model.*;
import models.CreateSubscriptionForm;
import play.Configuration;
import play.Logger;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TaxamoClient {

    static final String CARD_NUMBER_SUFFIX_KEY = "card-suffix";
    static final String PLAN_ID_KEY = "stripe-plan-id";

    private final TaxamoApi api;

    @Inject
    public TaxamoClient(@Named("taxamo") Configuration configuration) {
        this.api = new TaxamoApi(configuration.getString("private_api_key"));
    }

    public ListTransactionsOut getAllTransactions(String customKey) throws ApiException {
        return api.listTransactions(null, null, customKey, null, null, null, null, null, null, null, "0", null, null, null, null);
    }

    public String createPlaceholderTransaction(CreateSubscriptionForm createSubscriptionForm, String billingId) {
        CreateTransactionIn createTransactionIn = new CreateTransactionIn();

        List<InputTransactionLine> lines = new ArrayList<>();
        InputTransactionLine line = new InputTransactionLine();
        line.setCustomId(createSubscriptionForm.getPlan());
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
        transaction.setCurrencyCode("EUR");
        transaction.setBuyerIp(createSubscriptionForm.getBuyerInformation().getIp());
        transaction.setBuyerEmail(createSubscriptionForm.getBuyerInformation().getEmail());
        transaction.setBuyerName(createSubscriptionForm.getBuyerInformation().getName());
        InvoiceAddress address = new InvoiceAddress();
        address.setCountry(createSubscriptionForm.getBillingAddress().getCountryCode());
        address.setCity(createSubscriptionForm.getBillingAddress().getCity());
        address.setStreetName(createSubscriptionForm.getBillingAddress().getStreet());
        address.setPostalCode(createSubscriptionForm.getBillingAddress().getZipCode());
        transaction.setInvoiceAddress(address);
        transaction.setBillingCountryCode(createSubscriptionForm.getBillingAddress().getCountryCode());
        transaction.setCustomId(billingId);

        CustomFields planCustomField = new CustomFields();
        planCustomField.setKey(PLAN_ID_KEY);
        planCustomField.setValue(createSubscriptionForm.getPlan());

        CustomFields cardSuffixField = new CustomFields();
        cardSuffixField.setKey(CARD_NUMBER_SUFFIX_KEY);
        cardSuffixField.setValue(createSubscriptionForm.getBuyerInformation().getCardSuffix());

        transaction.setCustomFields(Arrays.asList(planCustomField, cardSuffixField));
        createTransactionIn.setTransaction(transaction);
        try {
            CreateTransactionOut transactionOut = api.createTransaction(createTransactionIn);

            ConfirmTransactionIn confirmTransactionIn = new ConfirmTransactionIn();
            ConfirmTransactionOut confirmTransactionOut = api.confirmTransaction(transactionOut.getTransaction().getKey(), confirmTransactionIn);
            return confirmTransactionOut.getTransaction().getKey();
        } catch (ApiException e) {
            Logger.error("Error creating placeholder transaction in taxamo: " + createSubscriptionForm + " for billing with id: " + billingId, e);
        }

        return "";
    }
}
