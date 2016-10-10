package controllers;

import akka.util.ByteString;
import com.fasterxml.jackson.databind.JsonNode;
import play.libs.F;
import play.libs.streams.Accumulator;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import javax.inject.Inject;
import java.util.concurrent.Executor;

class ReportRequestBodyParser implements BodyParser<ReportRequest> {
    private BodyParser.Json jsonParser;
    private Executor executor;

    @Inject
    public ReportRequestBodyParser(BodyParser.Json jsonParser, Executor executor) {
        this.jsonParser = jsonParser;
        this.executor = executor;
    }

    public Accumulator<ByteString, F.Either<Result, ReportRequest>> apply(Http.RequestHeader request) {
        Accumulator<ByteString, F.Either<Result, JsonNode>> jsonAccumulator = jsonParser.apply(request);
        return jsonAccumulator.map(resultOrJson -> {
            if (resultOrJson.left.isPresent()) {
                return F.Either.Left(resultOrJson.left.get());
            } else {
                JsonNode json = resultOrJson.right.get();
                try {
                    ReportRequest reportRequest = play.libs.Json.fromJson(json, ReportRequest.class);
                    return F.Either.Right(reportRequest);
                } catch (Exception e) {
                    return F.Either.Left(Results.badRequest(
                            "Unable to read " + ReportRequest.class.toString() + " from json: " + e.getMessage()));
                }
            }
        }, executor);
    }
}
