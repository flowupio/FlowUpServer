package usecases;

import play.libs.ws.WSResponse;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;


public class InsertDataPoints {

    private final MetricsDatasource metricsDatasource;

    @Inject
    public InsertDataPoints(MetricsDatasource metricsDatasource) {
        this.metricsDatasource = metricsDatasource;
    }

    public CompletionStage<WSResponse> execute() {
        return metricsDatasource.writeFakeCounter();
    }
}
