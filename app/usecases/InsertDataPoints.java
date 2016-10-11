package usecases;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;


public class InsertDataPoints {

    private final MetricsDatasource metricsDatasource;

    @Inject
    public InsertDataPoints(MetricsDatasource metricsDatasource) {
        this.metricsDatasource = metricsDatasource;
    }

    public CompletionStage<InsertResult> execute(List<Metric> metrics) {
        return metricsDatasource.writeDataPoints(metrics);
    }
}
