package usecases;

import datasources.taxamo.BillingDataSource;
import models.Organization;
import models.Transaction;
import play.Logger;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class GetBilling {

    private final BillingDataSource dataSource;

    @Inject
    public GetBilling(BillingDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public CompletionStage<List<Transaction>> execute(Organization organization) {
        return CompletableFuture.supplyAsync(() -> dataSource.getTransactions(organization.getBillingId()));
    }
}
