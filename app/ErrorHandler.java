import com.google.inject.Inject;
import error.AirbrakeErrorHandler;
import error.WithJsonErrorHandler;
import play.Configuration;
import play.Environment;
import play.Logger;
import play.api.OptionalSourceMapper;
import play.api.UsefulException;
import play.api.routing.Router;
import play.http.DefaultHttpErrorHandler;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Provider;
import java.util.Arrays;
import java.util.concurrent.CompletionStage;

public class ErrorHandler extends DefaultHttpErrorHandler implements WithJsonErrorHandler {

    private final Environment environment;
    private final AirbrakeErrorHandler airbrakeErrorHandler;

    @Inject
    public ErrorHandler(Configuration configuration, Environment environment, OptionalSourceMapper sourceMapper, Provider<Router> routes, AirbrakeErrorHandler airbrakeErrorHandler) {
        super(configuration, environment, sourceMapper, routes);
        this.environment = environment;
        this.airbrakeErrorHandler = airbrakeErrorHandler;
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
    public CompletionStage<Result> onProdServerError(Http.RequestHeader request, UsefulException exception) {
        return this.onProdServerError(request, exception, (requestHeader, usefulException) -> super.onProdServerError(requestHeader, usefulException));
    }

    @Override
    public CompletionStage<Result> onServerError(Http.RequestHeader request, Throwable exception) {
        logHeaders(request);
        return super.onServerError(request, exception);
    }

    @Override
    protected void logServerError(Http.RequestHeader request, UsefulException usefulException) {
        airbrakeErrorHandler.logServerError(request, usefulException, environment);
        super.logServerError(request, usefulException);
    }

    private void logHeaders(Http.RequestHeader request) {
        String headersString = request.headers().entrySet().stream().reduce(
                "Headers[",
                (acc, stringEntry) -> acc +=
                        stringEntry.getKey()
                                + ":"
                                + Arrays.stream(stringEntry.getValue()).reduce("", (s1, s2) -> s1 + " " + s2) + ", ",
                (s1, s2) -> s1 + s2) + "]";
        Logger.error("Server error: " + headersString);
    }
}
