package controllers;

import akka.util.ByteString;
import com.fasterxml.jackson.databind.JsonNode;
import datasources.database.ApiKeyDatasource;
import play.cache.CacheApi;
import play.libs.F;
import play.libs.streams.Accumulator;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import javax.inject.Inject;
import java.util.concurrent.Executor;

import static play.libs.F.Either.Left;
import static play.libs.F.Either.Right;

class ReportRequestBodyParser implements BodyParser<ReportRequest> {
    private final BodyParser.Json jsonParser;
    private final Executor executor;
    private final HeaderParsers headerParsers;

    @Inject
    public ReportRequestBodyParser(Json jsonParser, Executor executor, HeaderParsers headerParsers) {
        this.jsonParser = jsonParser;
        this.executor = executor;
        this.headerParsers = headerParsers;
    }

    public Accumulator<ByteString, F.Either<Result, ReportRequest>> apply(Http.RequestHeader request) {
        F.Either<Result, Http.RequestHeader> resultOrRequest = headerParsers.apply(request);
        if (resultOrRequest.left.isPresent()) {
            return Accumulator.done(Left(resultOrRequest.left.get()));
        }

        Accumulator<ByteString, F.Either<Result, JsonNode>> jsonAccumulator = jsonParser.apply(request);

        return jsonAccumulator.map(resultOrJson -> {
            if (resultOrJson.left.isPresent()) {
                return Left(resultOrJson.left.get());
            } else {
                JsonNode json = resultOrJson.right.get();
                return parse(json);
            }
        }, executor);
    }

    private F.Either<Result, ReportRequest> parse(JsonNode json) {
        try {
            ReportRequest reportRequest = play.libs.Json.fromJson(json, ReportRequest.class);
            return Right(reportRequest);
        } catch (Exception e) {
            Result result = Results.badRequest(
                    "Unable to read " + ReportRequest.class.toString() + " from json: " + e.getMessage());
            return Left(result);
        }
    }
}

class HeaderParsers {

    private CacheApi cache;
    private ApiKeyDatasource apiKeyDatasource;
    static String X_API_KEY = "X-Api-Key";

    @Inject
    HeaderParsers(CacheApi cache, ApiKeyDatasource apiKeyDatasource) {
        this.cache = cache;
        this.apiKeyDatasource = apiKeyDatasource;
    }

    private boolean apiKeyIsValid(String apiKey) {
        if (apiKey != null) {
            Boolean validApiKey = cache.get("apiKey.isValid." + apiKey);
            if (validApiKey != null) {
                return validApiKey;
            } else {
                boolean valuePresentInDB = apiKeyDatasource.isValuePresentInDB(apiKey);
                cache.set("apiKey.isValid." + apiKey, valuePresentInDB);
                return valuePresentInDB;
            }
        } else {
            return false;
        }
    }

    private boolean apiKeyIsEnabled(String apiKey) {
        if (apiKey != null) {
            Boolean validApiKey = cache.get("apiKey.isEnabled." + apiKey);
            if (validApiKey != null) {
                return validApiKey;
            } else {
                boolean isApiKeyEnabled = apiKeyDatasource.isApiKeyEnabled(apiKey);
                cache.set("apiKey.isEnabled." + apiKey, isApiKeyEnabled);
                return isApiKeyEnabled;
            }
        } else {
            return false;
        }
    }

    F.Either<Result, Http.RequestHeader> apply(Http.RequestHeader request) {
        String apiKey = request.getHeader(X_API_KEY);
        boolean apiKeyIsValid = apiKeyIsValid(apiKey);
        boolean apiKeyIsEnabled = apiKeyIsEnabled(apiKey);
        if (apiKeyIsValid && apiKeyIsEnabled) {
            return Right(request);
        } else if (apiKeyIsValid && !apiKeyIsEnabled) {
            return Left(Results.status(Http.Status.PRECONDITION_FAILED, "API KEY disabled"));
        } else {
            return Left(Results.unauthorized("Expected Valid API KEY"));
        }
    }
}
