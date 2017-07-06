package datasources.billing;

import com.stripe.model.Customer;
import models.CreateSubscriptionRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class StripeSubscriptionMapper {

    public Map<String, Object> mapNewCustomer(CreateSubscriptionRequest createSubscriptionRequest) {
        Map<String, Object> newCustomerParams = new HashMap<>();
        newCustomerParams.put("email", createSubscriptionRequest.getBuyerInformation().getEmail());
        newCustomerParams.put("source", createSubscriptionRequest.getToken());
        return newCustomerParams;
    }

    public Map<String, Object> mapNewSubscription(CreateSubscriptionRequest createSubscriptionRequest, Customer customer, String transactionKey) {
        Map<String, Object> newSubscriptionParams = new HashMap<>();
        newSubscriptionParams.put("customer", customer.getId());
        newSubscriptionParams.put("plan", createSubscriptionRequest.getPlan());
        newSubscriptionParams.put("quantity", createSubscriptionRequest.getQuantity());
        newSubscriptionParams.put("metadata", Collections.singletonMap("taxamo_transaction_key", transactionKey));
        return newSubscriptionParams;
    }
}
