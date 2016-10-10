package usecases;

import lombok.Data;

import java.util.List;

@Data
public class Metric {
    private final String name;
    private final List<DataPoint> dataPoints;
}
