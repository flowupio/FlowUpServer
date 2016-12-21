package usecases.models;

import lombok.Data;
import lombok.experimental.Accessors;
import usecases.models.Metric;

import java.util.List;

@Data
public class Report {
    private final String apiKey;
    private final String appPackage;
    private final List<Metric> metrics;
    private final Boolean inDebugMode;
    private final Boolean appInBackground;
}
