package datasources.billing;

import com.taxamo.client.model.*;
import models.CreateSubscriptionRequest;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TaxamoTransactionMapper {

    private static final String TAXAMO_PLAN_CURRENCY = "EUR";
    private static final String BILLING_SERVICE = "stripe";
    private static final String PLACEHOLDER_TRANSACTION_KEY = "placeholder-transaction";

    public CreateTransactionIn map(CreateSubscriptionRequest createSubscriptionRequest, String billingId) {
        CreateTransactionIn createTransactionIn = new CreateTransactionIn();
        createTransactionIn.setTransaction(mapInputTransaction(createSubscriptionRequest, billingId));
        return createTransactionIn;
    }

    @NotNull
    private List<CustomFields> mapCustomFields(CreateSubscriptionRequest createSubscriptionRequest) {
        CustomFields planCustomField = new CustomFields();
        planCustomField.setKey(TaxamoClient.PLAN_ID_KEY);
        planCustomField.setValue(createSubscriptionRequest.getPlan());

        CustomFields cardSuffixField = new CustomFields();
        cardSuffixField.setKey(TaxamoClient.CARD_NUMBER_SUFFIX_KEY);
        cardSuffixField.setValue(createSubscriptionRequest.getBuyerInformation().getCardSuffix());

        return Arrays.asList(planCustomField, cardSuffixField);
    }

    @NotNull
    private InputTransaction mapInputTransaction(CreateSubscriptionRequest createSubscriptionRequest, String billingId) {
        List<InputTransactionLine> lines = mapTransactionLines(createSubscriptionRequest);
        List<CustomFields> fields = mapTransactionCustomFields();
        List<CustomFields> customFields = mapCustomFields(createSubscriptionRequest);

        InputTransaction transaction = new InputTransaction();
        transaction.setTransactionLines(lines);
        transaction.setCustomFields(fields);
        transaction.setCurrencyCode(TAXAMO_PLAN_CURRENCY);
        transaction.setBuyerIp(createSubscriptionRequest.getBuyerInformation().getIp());
        transaction.setBuyerEmail(createSubscriptionRequest.getBuyerInformation().getEmail());
        transaction.setBuyerName(createSubscriptionRequest.getBuyerInformation().getName());
        transaction.setInvoiceAddress(mapInvoiceAddress(createSubscriptionRequest));
        transaction.setBillingCountryCode(createSubscriptionRequest.getBillingAddress().getCountryCode());
        transaction.setCustomId(billingId);
        transaction.setCustomFields(customFields);

        return transaction;
    }

    @NotNull
    private InvoiceAddress mapInvoiceAddress(CreateSubscriptionRequest createSubscriptionRequest) {
        InvoiceAddress address = new InvoiceAddress();
        address.setCountry(createSubscriptionRequest.getBillingAddress().getCountryCode());
        address.setCity(createSubscriptionRequest.getBillingAddress().getCity());
        address.setStreetName(createSubscriptionRequest.getBillingAddress().getStreet());
        address.setPostalCode(createSubscriptionRequest.getBillingAddress().getZipCode());
        return address;
    }

    @NotNull
    private List<CustomFields> mapTransactionCustomFields() {
        List<CustomFields> fields = new ArrayList<>();
        CustomFields field = new CustomFields();
        field.setKey(PLACEHOLDER_TRANSACTION_KEY);
        field.setValue(BILLING_SERVICE);
        fields.add(field);
        return fields;
    }

    @NotNull
    private List<InputTransactionLine> mapTransactionLines(CreateSubscriptionRequest createSubscriptionRequest) {
        List<InputTransactionLine> lines = new ArrayList<>();
        InputTransactionLine line = new InputTransactionLine();
        line.setCustomId(createSubscriptionRequest.getPlan());
        line.setAmount(BigDecimal.ZERO);
        lines.add(line);
        return lines;
    }
}
