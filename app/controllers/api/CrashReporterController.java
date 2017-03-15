package controllers.api;

import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import security.ApiKeySecuredAction;
import usecases.DeleteYesterdayAllowedUUIDs;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@With(ApiKeySecuredAction.class)
public class CrashReporterController extends Controller {

    @Inject
    public CrashReporterController() {

    }

    public CompletionStage<Result> reportClientError() {

        return CompletableFuture.supplyAsync(() -> {

            return ok();
        });
    }
}
