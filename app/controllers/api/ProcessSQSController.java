package controllers.api;

import com.google.inject.Inject;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import usecases.ProcessReportQueue;

import java.util.concurrent.CompletionStage;

public class ProcessSQSController extends Controller {

    private final ProcessReportQueue processReportQueue;

    @Inject
    public ProcessSQSController(ProcessReportQueue processReportQueue) {
        this.processReportQueue = processReportQueue;
    }

    public CompletionStage<Result> index() {
        return processReportQueue.execute().thenApply(success -> ok());
    }
}

