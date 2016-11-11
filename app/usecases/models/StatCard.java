package usecases.models;

public class StatCard {
    private final String description;
    private final Double number;
    private final String unit;
    private final LineChart lineChart;

    public StatCard(String description, Double number, String unit, LineChart lineChart) {
        this.description = description;
        this.number = number;
        this.unit = unit;
        this.lineChart = lineChart;
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

    public String getUnit() {
        return unit;
    }
}
