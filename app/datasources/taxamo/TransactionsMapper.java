package datasources.taxamo;

import com.taxamo.client.model.CustomFields;
import com.taxamo.client.model.Transactions;
import org.jetbrains.annotations.NotNull;
import usecases.models.Transaction;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

class TransactionsMapper {

    private static final String KEY_BRAINTREE_PLAN_ID = "braintree-original-plan-id";

    Transaction mapTransaction(Transactions taxamoTransaction) {
        return new Transaction(
                taxamoTransaction.getKey(),
                mapCreationTimestamp(taxamoTransaction.getCreateTimestamp()),
                mapPlan(taxamoTransaction),
                taxamoTransaction.getBuyerCreditCardPrefix(),
                taxamoTransaction.getCurrencyCode(),
                taxamoTransaction.getTotalAmount(),
                taxamoTransaction.getInvoiceImageUrl()
        );
    }

    @NotNull
    String mapPlan(Transactions taxamoTransaction) {
        String braintreePlanId = taxamoTransaction.getCustomFields().stream()
                .filter(item -> KEY_BRAINTREE_PLAN_ID.equals(item.getKey()))
                .findFirst()
                .map(CustomFields::getValue)
                .orElse("unknown");

        switch (braintreePlanId) {
            case "company_plan":
            case "company_plan_untaxed":
                return "Company";
            case "enterprise_plan":
            case "enterprise_plan_untaxed":
                return "Enterprise";
            default:
                return "Unknown";
        }
    }

    @NotNull
    private String mapCreationTimestamp(String creationTimestamp) {
        return LocalDate
                .parse(creationTimestamp, DateTimeFormatter.ISO_DATE_TIME)
                .format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
