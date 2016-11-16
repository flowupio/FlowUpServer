import com.google.inject.Inject;
import datasources.grafana.HttpServerErrorHandler;
import play.Configuration;
import play.Environment;
import play.api.OptionalSourceMapper;
import play.api.UsefulException;
import play.api.routing.Router;
import play.http.DefaultHttpErrorHandler;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Provider;
import java.util.concurrent.CompletionStage;

public class ErrorHandler extends DefaultHttpErrorHandler implements WithJsonErrorHandlerHandler {
    @Inject
    public ErrorHandler(Configuration configuration, Environment environment, OptionalSourceMapper sourceMapper, Provider<Router> routes) {
        super(configuration, environment, sourceMapper, routes);
    }

    @Override
    public CompletionStage<Result> onClientError(Http.RequestHeader request, int statusCode, String message) {
        return this.onClientError(request, statusCode, message, super::onClientError);
    }

    @Override
    protected CompletionStage<Result> onDevServerError(Http.RequestHeader request, UsefulException exception) {
        return this.onDevServerError(request, exception, (requestHeader, usefulException) -> super.onDevServerError(requestHeader, usefulException));
    }

    @Override
    public CompletionStage<Result> onProdServerError(Http.RequestHeader request, UsefulException exception, HttpServerErrorHandler httpServerErrorHandler) {
        return this.onProdServerError(request, exception, (requestHeader, usefulException) -> super.onProdServerError(requestHeader, usefulException));
    }
}
