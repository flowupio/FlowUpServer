package controllers;

import akka.util.ByteString;
import com.fasterxml.jackson.databind.JsonNode;
import models.ApiKey;
import play.libs.F;
import play.libs.streams.Accumulator;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.Executor;

import static play.libs.F.Either.Left;
import static play.libs.F.Either.Right;

class ReportRequestBodyParser implements BodyParser<ReportRequest> {
    private BodyParser.Json jsonParser;
    private Executor executor;

    @Inject
    public ReportRequestBodyParser(BodyParser.Json jsonParser, Executor executor) {
        this.jsonParser = jsonParser;
        this.executor = executor;
    }

    public Accumulator<ByteString, F.Either<Result, ReportRequest>> apply(Http.RequestHeader request) {
        F.Either<Result, Http.RequestHeader> resultOrRequest = HeaderParsers.apply(request);
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

    private static String X_API_KEY = "X-Api-Key";

    private static boolean apiKeyIsValid(Http.RequestHeader request) {
        Optional<String> apiKey = Optional.ofNullable(request.getHeader(X_API_KEY));
        return apiKey.map(apiKeyValue -> ApiKey.find.where().eq("value", apiKeyValue).findRowCount() > 0).orElse(false);
    }

    static F.Either<Result, Http.RequestHeader> apply(Http.RequestHeader request) {
        if (apiKeyIsValid(request)) {
            return Right(request);
        } else {
            return Left(Results.unauthorized("Expected Valid API KEY"));
        }
    }
}
