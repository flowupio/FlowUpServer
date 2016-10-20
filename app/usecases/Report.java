package usecases;

import lombok.Data;

import java.util.List;

@Data
public class Report {
    private final String orgnizationIdentifier;
    private final String appPackage;
    private final List<Metric> metrics;
}
