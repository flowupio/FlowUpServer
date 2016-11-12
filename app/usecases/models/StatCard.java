package usecases.models;

public class StatCard {
    private final String description;
    private final Double number;
    private final Unit unit;
    private final LineChart lineChart;
    private final Threshold threshold;

    public StatCard(String description, Double number, Unit unit, LineChart lineChart, Threshold threshold) {
        this.description = description;
        this.number = number;
        this.unit = unit;
        this.lineChart = lineChart;
        this.threshold = threshold;
    }

    public String getDescription() {
        return description;
    }

    public Double getNumber() {
        return number;
    }

    public LineChart getLineChart() {
        return lineChart;
    }

    public Unit getUnit() {
        return unit;
    }

    public Threshold getThreshold() {
        return threshold;
    }
}

