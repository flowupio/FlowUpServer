package usecases.models;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Transaction {
    private final String creditCardNumber;
    private final BigDecimal amount;
}
