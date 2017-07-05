package datasources.billing;

import com.taxamo.client.model.CustomFields;
import com.taxamo.client.model.Transactions;
import org.jetbrains.annotations.NotNull;
import usecases.models.Transaction;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

class TransactionsMapper {

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
        String plan = taxamoTransaction.getCustomFields().stream()
                .filter(item -> TaxamoClient.PLAN_ID_KEY.equals(item.getKey()))
                .findFirst()
                .map(CustomFields::getValue)
                .orElse("unknown");

        switch (plan) {
            case "ProPlan":
                return "Professional";
            case "BizPlan":
                return "Business";
            default:
                return "Developer";
        }
    }

    @NotNull
    private String mapCreationTimestamp(String creationTimestamp) {
        return LocalDate
                .parse(creationTimestamp, DateTimeFormatter.ISO_DATE_TIME)
                .format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
