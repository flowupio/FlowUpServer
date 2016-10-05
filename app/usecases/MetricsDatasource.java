package usecases;

import play.libs.ws.WSResponse;

import java.util.concurrent.CompletionStage;

public interface MetricsDatasource {
    CompletionStage<WSResponse> writeFakeCounter();
}
