package models;

import java.math.BigDecimal;

public class Transaction {

    private final String creditCardNumber;
    private final BigDecimal amount;

    public Transaction(String creditCardNumberPrefix, BigDecimal amount) {
        this.creditCardNumber = creditCardNumberPrefix;
        this.amount = amount;
    }

    public String getCreditCardNumber() {
        return creditCardNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
