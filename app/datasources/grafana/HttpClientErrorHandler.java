package datasources.grafana;

import play.mvc.Http;
import play.mvc.Result;

import java.util.concurrent.CompletionStage;

public interface HttpClientErrorHandler {
    CompletionStage<Result> onClientError(Http.RequestHeader request, int statusCode, String message);
}

