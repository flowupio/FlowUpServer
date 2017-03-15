package controllers.api;

import play.libs.Json;
import play.mvc.*;
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
        Http.RequestBody body = request().body();
        ErrorReport errorReport = Json.fromJson(body.asJson(), ErrorReport.class);
        return CompletableFuture.supplyAsync(() -> {
            reportClientError.execute(errorReport);
            return created(Json.toJson(errorReport));
        });
    }
}
