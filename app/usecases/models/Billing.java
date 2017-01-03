package usecases.models;

import lombok.Data;

import java.util.List;

@Data
public class Billing {
    private final List<Transaction> transactions;
}
