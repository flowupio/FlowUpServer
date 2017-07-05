package models;

import lombok.Data;

@Data
public class BillingAddress {
    private String countryCode;
    private String city;
    private String zipCode;
    private String street;
}
