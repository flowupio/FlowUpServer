package models;

import lombok.Data;

@Data
public class CreateSubscriptionRequest {
    private BuyerInformation buyerInformation;
    private BillingAddress billingAddress;
    private String token;
    private String plan;
    private String quantity;
}