package controllers.api;

import com.google.inject.Inject;
import play.mvc.Controller;
import play.mvc.Result;
import usecases.SendPulseToAllApplications;

import java.util.concurrent.CompletionStage;

public class FindBugsController extends Controller {

    private final SendPulseToAllApplications sendPulseToAllApplications;

    @Inject
    public FindBugsController(SendPulseToAllApplications sendPulseToAllApplications) {
        this.sendPulseToAllApplications = sendPulseToAllApplications;
    }

    public CompletionStage<Result> index() {
        return sendPulseToAllApplications.execute().thenApply(booleen -> ok());
    }
}
