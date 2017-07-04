package models;

import lombok.Data;

@Data
public class CreateSubscriptionForm {
    private String email;
    private String token;
    private String plan;
    private String quantity;
    private String country;
    private String buyerIp;
}
