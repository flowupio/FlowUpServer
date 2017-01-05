package usecases;

import datasources.taxamo.BillingDataSource;
import models.Organization;
import usecases.models.Billing;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class GetBillingInformation {

    private final BillingDataSource dataSource;

    @Inject
    public GetBillingInformation(BillingDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public CompletionStage<Billing> execute(Organization organization) {
        return dataSource.getBilling(organization.getBillingId());
    }
}
