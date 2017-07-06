package datasources.billing;

import com.google.inject.name.Named;
import com.stripe.Stripe;
import com.stripe.exception.*;
import com.stripe.model.Customer;
import com.stripe.model.Subscription;
import models.CreateSubscriptionRequest;
import play.Configuration;
import play.Logger;

import javax.inject.Inject;

public class StripeClient {

    private final StripeSubscriptionMapper mapper;

    @Inject
    public StripeClient(@Named("stripe") Configuration configuration, StripeSubscriptionMapper mapper) {
        Stripe.apiKey = configuration.getString("private_api_key");
        this.mapper = mapper;
    }

    public boolean createSubscription(CreateSubscriptionRequest createSubscriptionRequest, String transactionKey) {
        try {
            Customer customer = Customer.create(mapper.mapNewCustomer(createSubscriptionRequest));
            Subscription.create(mapper.mapNewSubscription(createSubscriptionRequest, customer, transactionKey));
            return true;
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException | APIException e) {
            Logger.error("Unable to create stripe subscription: " + transactionKey);
            return false;
        }
    }
}
