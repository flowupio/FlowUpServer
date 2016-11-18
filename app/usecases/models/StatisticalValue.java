package usecases.models;

import lombok.Data;

@Data
public class StatisticalValue implements Value {
    private final double mean;
    private final double p10;
    private final double p90;
}
