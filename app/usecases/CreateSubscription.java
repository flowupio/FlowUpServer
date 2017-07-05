package usecases;

import datasources.billing.BillingDataSource;
import models.CreateSubscriptionRequest;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class CreateSubscription {

    private final BillingDataSource billingDataSource;

    @Inject
    public CreateSubscription(BillingDataSource billingDataSource) {
        this.billingDataSource = billingDataSource;
    }

    public CompletionStage<Boolean> createSubscription(CreateSubscriptionRequest createSubscriptionRequest, String billingId) {
        return billingDataSource.createSubscription(createSubscriptionRequest, billingId);
    }
}
