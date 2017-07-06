package controllers.api;

import akka.util.ByteString;
import com.fasterxml.jackson.databind.JsonNode;
import models.CreateSubscriptionRequest;
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

public class CreateSubscriptionBodyParser implements BodyParser<CreateSubscriptionRequest> {

    private final BodyParser.Json jsonParser;
    private final Executor executor;

    @Inject
    public CreateSubscriptionBodyParser(BodyParser.Json jsonParser, Executor executor) {
        this.jsonParser = jsonParser;
        this.executor = executor;
    }

    @Override
    public Accumulator<ByteString, F.Either<Result, CreateSubscriptionRequest>> apply(Http.RequestHeader request) {
        Accumulator<ByteString, F.Either<Result, JsonNode>> jsonAccumulator = jsonParser.apply(request);

        return jsonAccumulator.map(resultOrJson -> {
            if (resultOrJson.left.isPresent()) {
                return Left(Results.badRequest());
            } else {
                JsonNode json = resultOrJson.right.get();
                return parse(json);
            }
        }, executor);
    }

    private F.Either<Result, CreateSubscriptionRequest> parse(JsonNode json) {
        try {
            CreateSubscriptionRequest reportRequest = play.libs.Json.fromJson(json, CreateSubscriptionRequest.class);
            return Right(reportRequest);
        } catch (Exception e) {
            Result result = Results.badRequest(
                    "Unable to read " + CreateSubscriptionRequest.class.toString() + " from json: " + e.getMessage());
            return Left(result);
        }
    }
}
