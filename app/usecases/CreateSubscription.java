package usecases;

import datasources.billing.BillingDataSource;
import models.CreateSubscriptionForm;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class CreateSubscription {

    private final BillingDataSource billingDataSource;

    @Inject
    public CreateSubscription(BillingDataSource billingDataSource) {
        this.billingDataSource = billingDataSource;
    }

    public CompletionStage<Boolean> createSubscription(CreateSubscriptionForm createSubscriptionForm, String billingId) {
        return billingDataSource.createSubscription(createSubscriptionForm, billingId);
    }
}
