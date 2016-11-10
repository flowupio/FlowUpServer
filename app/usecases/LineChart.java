package usecases;

import lombok.Data;

import java.util.List;

public class LineChart {
    private final List<String> labels;
    private final List<Double> values;

    public LineChart(List<String> labels, List<Double> values) {
        this.labels = labels;
        this.values = values;
    }

    public List<String> getLabels() {
        return labels;
    }

    public List<Double> getValues() {
        return values;
    }
}

@Data
class StatCard {
    private final String description;
    private final Double number;
    private final LineChart lineChart;
}
