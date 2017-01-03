package usecases;

import datasources.taxamo.BillingDataSource;
import models.Organization;
import usecases.models.Billing;
import usecases.models.Transaction;

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

    public CompletionStage<Billing> execute(Organization organization) {
        return dataSource.getBilling(organization.getBillingId());
    }
}
