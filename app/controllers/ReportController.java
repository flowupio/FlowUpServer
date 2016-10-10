package controllers;

import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import usecases.InsertDataPoints;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class ReportController extends Controller {
    @Inject
    InsertDataPoints insertDataPoints;

    @BodyParser.Of(ReportRequestBodyParser.class)
    public CompletionStage<Result> index() {
        Http.RequestBody body = request().body();
        ReportRequest reportRequest = body.as(ReportRequest.class);

        return insertDataPoints.execute().thenApply(response -> {
                    ReportResponse reportResponse = new ReportResponse();
                    reportResponse.message = "Metrics Inserted";
                    return created(Json.toJson(reportResponse));
                }
        );
    }
}
