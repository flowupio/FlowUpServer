package controllers.api;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import security.ApiKeySecuredAction;
import usecases.ReportClientError;
import usecases.models.ErrorReport;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@With(ApiKeySecuredAction.class)
public class CrashReporterController extends Controller {

    private final ReportClientError reportClientError;

    @Inject
    public CrashReporterController(ReportClientError reportClientError) {
        this.reportClientError = reportClientError;
    }

    public CompletionStage<Result> reportClientError() {
        return CompletableFuture.supplyAsync(() -> {
            Http.RequestBody body = request().body();
            ErrorReport errorReport = body.as(ErrorReport.class);
            reportClientError.execute(errorReport);
            return created(Json.toJson(errorReport));
        });
    }
}
