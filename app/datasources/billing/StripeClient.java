package datasources.billing;

import com.google.inject.name.Named;
import com.stripe.Stripe;
import com.stripe.exception.*;
import com.stripe.model.Customer;
import com.stripe.model.Subscription;
import models.CreateSubscriptionForm;
import play.Configuration;
import play.Logger;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class StripeClient {

    @Inject
    public StripeClient(@Named("stripe") Configuration configuration) {
        Stripe.apiKey = configuration.getString("private_api_key");
    }

    public void createSubscription(CreateSubscriptionForm createSubscriptionForm, String transactionKey) {
        Map<String, Object> newCustomerParams = new HashMap<>();
        newCustomerParams.put("email", createSubscriptionForm.getBuyerInformation().getEmail());
        newCustomerParams.put("source", createSubscriptionForm.getToken());

        try {
            Customer customer = Customer.create(newCustomerParams);

            Map<String, Object> newSubscriptionParams = new HashMap<>();
            newSubscriptionParams.put("customer", customer.getId());
            newSubscriptionParams.put("plan", createSubscriptionForm.getPlan());
            newSubscriptionParams.put("quantity", createSubscriptionForm.getQuantity());
            newSubscriptionParams.put("metadata", Collections.singletonMap("taxamo_transaction_key", transactionKey));
            Logger.debug("Creating subscription...");
            Subscription.create(newSubscriptionParams);
        } catch (AuthenticationException | InvalidRequestException | APIConnectionException | CardException | APIException e) {
            e.printStackTrace();
        }
    }
}
