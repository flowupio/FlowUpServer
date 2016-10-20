package usecases;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;


public class InsertDataPoints {

    private final MetricsDatasource metricsDatasource;

    @Inject
    public InsertDataPoints(MetricsDatasource metricsDatasource) {
        this.metricsDatasource = metricsDatasource;
    }

    public CompletionStage<InsertResult> execute(Report report) {
        return metricsDatasource.writeDataPoints(report);
    }
}
