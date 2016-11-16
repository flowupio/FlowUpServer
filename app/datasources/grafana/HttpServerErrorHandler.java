package datasources.grafana;

import play.api.UsefulException;
import play.mvc.Http;
import play.mvc.Result;

import java.util.concurrent.CompletionStage;

public interface HttpServerErrorHandler {
    CompletionStage<Result> onServerError(Http.RequestHeader request, UsefulException exception);
}
