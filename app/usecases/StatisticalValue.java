package usecases;

import lombok.Data;

@Data
public class StatisticalValue implements Value {
    private final long count;
    private final double min;
    private final double max;
    private final double mean;
    private final double standardDev;
    private final double median;
    private final double p1;
    private final double p2;
    private final double p5;
    private final double p10;
    private final double p80;
    private final double p95;
    private final double p98;
    private final double p99;
}
