package usecases.models;

import lombok.Data;
import usecases.models.DataPoint;

import java.util.List;

@Data
public class Metric {
    private final String name;
    private final List<DataPoint> dataPoints;
}
