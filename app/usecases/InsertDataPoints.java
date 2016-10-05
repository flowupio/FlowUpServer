package usecases;

import play.libs.ws.WSResponse;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;


/**
 * Created by davide on 05/10/16.
 */
public class InsertDataPoints {

    @Inject
    MetricsDatasource metricsDatasource;

    public CompletionStage<WSResponse> execute() {
        return metricsDatasource.writeFakeCounter();
    }
}
