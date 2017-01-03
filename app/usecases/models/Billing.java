package usecases.models;

import lombok.Data;

import java.util.List;

@Data
public class Billing {
    private final String fullName;
    private final String email;
    private final String address;
    private final String postalCode;
    private final String city;
    private final String country;

    private final String cardNumber;
    private final String currentPlan;

    private final List<Transaction> transactions;
}
