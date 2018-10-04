package usecases.models;

import java.math.BigDecimal;

public class Transaction {
    private final String id;
    private final String creationDate;
    private final String plan;
    private final String creditCardNumber;
    private final String currency;
    private final BigDecimal amount;
    private final String invoiceUrl;

    public Transaction(String id, String creationDate, String plan, String creditCardNumber, String currency,
                       BigDecimal amount, String invoiceUrl) {
        this.id = id;
        this.creationDate = creationDate;
        this.plan = plan;
        this.creditCardNumber = creditCardNumber;
        this.currency = currency;
        this.amount = amount;
        this.invoiceUrl = invoiceUrl;
    }

    public String getId() {
        return id;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public String getPlan() {
        return plan;
    }

    public String getCreditCardNumber() {
        return creditCardNumber;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getInvoiceUrl() {
        return invoiceUrl;
    }
}
