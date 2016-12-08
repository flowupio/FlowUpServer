package usecases;

import com.google.inject.Inject;

import java.util.concurrent.CompletionStage;

public class ProcessReportQueue {
    private final MetricsDatasource metricsDatasource;

    @Inject
    ProcessReportQueue(MetricsDatasource metricsDatasource) {
        this.metricsDatasource = metricsDatasource;
    }

    public CompletionStage<Boolean> execute() {
        return metricsDatasource.processSQS();
    }
}
