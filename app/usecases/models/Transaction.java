package usecases.models;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Transaction {
    private final String id;
    private final String creationDate;
    private final String plan;
    private final String creditCardNumber;
    private final String currency;
    private final BigDecimal amount;
    private final String invoiceUrl;
}
