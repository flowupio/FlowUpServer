package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import usecases.InsertDataPoints;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class ReportController extends Controller {
    @Inject
    InsertDataPoints insertDataPoints;

    public CompletionStage<Result> index() {
        return insertDataPoints.execute().thenApply(response ->
                ok("Metric Inserted \n" + "elastic response: " + response.getBody())
        );
    }
}
