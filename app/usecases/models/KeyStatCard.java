package usecases.models;

import lombok.Data;

import java.util.List;

@Data
public class KeyStatCard {
    private final StatCard main;
    private List<StatCard> detail;
}
