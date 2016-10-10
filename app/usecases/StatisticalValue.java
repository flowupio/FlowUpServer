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
    private final double p5;
    private final double p10;
    private final double p15;
    private final double p20;
    private final double p25;
    private final double p30;
    private final double p40;
    private final double p50;
    private final double p60;
    private final double p70;
    private final double p75;
    private final double p80;
    private final double p85;
    private final double p90;
    private final double p95;
    private final double p98;
    private final double p99;
}
