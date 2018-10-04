package controllers.api;

import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import usecases.DeleteOldDataPoints;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class ElasticsearchController extends Controller {

    private final DeleteOldDataPoints deleteOldDataPoints;

    @Inject
    public ElasticsearchController(DeleteOldDataPoints deleteOldDataPoints) {
        this.deleteOldDataPoints = deleteOldDataPoints;
    }

    @BodyParser.Of(SNSMessageBodyParser.class)
    public CompletionStage<Result> deleteOldDataPoints() {
        return deleteOldDataPoints.execute().thenApply(result -> ok());
    }
}
