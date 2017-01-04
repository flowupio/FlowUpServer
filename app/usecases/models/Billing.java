package usecases.models;

import java.util.List;

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

    public Billing(String fullName, String email, String address, String postalCode, String city, String country,
                   String cardNumber, String currentPlan, List<Transaction> transactions) {
        this.fullName = fullName;
        this.email = email;
        this.address = address;
        this.postalCode = postalCode;
        this.city = city;
        this.country = country;
        this.cardNumber = cardNumber;
        this.currentPlan = currentPlan;
        this.transactions = transactions;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getCurrentPlan() {
        return currentPlan;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }
}
