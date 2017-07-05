package models;

import lombok.Data;

@Data
public class BuyerInformation {
    private String email;
    private String name;
    private String cardSuffix;
    private String ip;
}
