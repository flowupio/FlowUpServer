package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import usecases.InsertDataPoints;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class ReportController extends Controller {
    @Inject
    InsertDataPoints insertDataPoints;

    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> index() {
        JsonNode json = request().body().asJson();
        ReportRequest reportRequest = Json.fromJson(json, ReportRequest.class);

        return insertDataPoints.execute().thenApply(response -> {
                    ReportResponse reportResponse = new ReportResponse();
                    reportResponse.message = "Metrics Inserted";
                    return ok(Json.toJson(reportResponse));
                }
        );
    }
}
