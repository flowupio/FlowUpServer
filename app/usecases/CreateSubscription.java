package usecases;

import datasources.billing.BillingDataSource;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class CreateSubscription {

    private final BillingDataSource billingDataSource;

    @Inject
    public CreateSubscription(BillingDataSource billingDataSource) {
        this.billingDataSource = billingDataSource;
    }

    public CompletionStage<Boolean> createSubscription(String email, String country, String buyerIp, String token, String plan, int quantity) {
        return billingDataSource.createSubscription(email, country, buyerIp, token, plan, quantity);
    }
}
