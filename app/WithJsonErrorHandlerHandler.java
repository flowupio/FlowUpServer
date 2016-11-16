import datasources.grafana.HttpClientErrorHandler;
import datasources.grafana.HttpServerErrorHandler;
import play.api.UsefulException;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

interface WithJsonErrorHandlerHandler {

    default CompletionStage<Result> onClientError(Http.RequestHeader request, int statusCode, String message, HttpClientErrorHandler httpClientErrorHandler) {
        if (isContentTypeJson(request)) {
            if (statusCode == 400) {
                return CompletableFuture.completedFuture(Results.badRequest(Json.newObject()
                        .put("method", request.method())
                        .put("uri", request.uri())
                        .put("message", message)
                ));
            } else if (statusCode == 403) {
                return CompletableFuture.completedFuture(Results.forbidden(Json.newObject()
                        .put("message", "You must be authenticated to access this page.")));
            } else if (statusCode == 404) {
                return CompletableFuture.completedFuture(Results.notFound(Json.newObject()
                        .put("method", request.method())
                        .put("uri", request.uri())));
            } else if (statusCode >= 400 && statusCode < 500) {
                return CompletableFuture.completedFuture(Results.status(statusCode, Json.newObject()
                        .put("method", request.method())
                        .put("uri", request.uri())
                        .put("message", message)
                ));
            } else {
                throw new IllegalArgumentException("onClientError invoked with non client error status code " + statusCode + ": " + message);
            }
        } else {
            return httpClientErrorHandler.onClientError(request, statusCode, message);
        }
    }

    default CompletionStage<Result> onDevServerError(Http.RequestHeader request, UsefulException exception, HttpServerErrorHandler httpServerErrorHandler) {
        if (isContentTypeJson(request)) {
            return CompletableFuture.completedFuture(Results.internalServerError(Json.toJson(exception)));
        } else {
            return httpServerErrorHandler.onServerError(request, exception);
        }
    }

    default CompletionStage<Result> onProdServerError(Http.RequestHeader request, UsefulException exception, HttpServerErrorHandler httpServerErrorHandler) {
        if (isContentTypeJson(request)) {
            return CompletableFuture.completedFuture(Results.internalServerError(Json.newObject().set("exception", Json.newObject().put("id", exception.id))));
        } else {
            return httpServerErrorHandler.onServerError(request, exception);
        }
    }

    default boolean isContentTypeJson(Http.RequestHeader request) {
        return request.contentType().map(this::isJsonHeader).orElse(false);
    }

    default boolean isJsonHeader(String contentType) {
        return contentType.equalsIgnoreCase("application/json") || contentType.equalsIgnoreCase("text/json");
    }
}
