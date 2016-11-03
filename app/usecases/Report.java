package usecases;

import lombok.Data;

import java.util.List;

@Data
public class Report {
    private final String apiKey;
    private final String appPackage;
    private final List<Metric> metrics;
}
