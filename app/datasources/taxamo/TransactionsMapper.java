package datasources.taxamo;

import com.taxamo.client.model.Transactions;
import usecases.models.Transaction;

public class TransactionsMapper {

    public Transaction mapTransaction(Transactions taxamoTransaction) {
        return new Transaction(
                taxamoTransaction.getBuyerCreditCardPrefix(),
                taxamoTransaction.getAmount()
        );
    }
}
