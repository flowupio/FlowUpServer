package usecases.models;

import lombok.Data;
import usecases.models.Metric;

import java.util.List;

@Data
public class Report {
    private final String apiKey;
    private final String appPackage;
    private final List<Metric> metrics;
}
