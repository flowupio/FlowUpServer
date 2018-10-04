package controllers.api;

import play.libs.Json;
import play.mvc.*;
import security.ApiKeySecuredAction;
import usecases.ReportClientError;
import usecases.models.ErrorReport;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static controllers.api.HeaderParsers.X_API_KEY;

@With(ApiKeySecuredAction.class)
public class CrashReporterController extends Controller {

    private final ReportClientError reportClientError;

    @Inject
    public CrashReporterController(ReportClientError reportClientError) {
        this.reportClientError = reportClientError;
    }

    public CompletionStage<Result> reportClientError() {
        Http.Request request = request();
        Http.RequestBody body = request().body();
        ErrorReport errorReport = Json.fromJson(body.asJson(), ErrorReport.class);
        errorReport.setLibraryVersion(request.getHeader(USER_AGENT));
        errorReport.setApiKey(request.getHeader(X_API_KEY));
        return CompletableFuture.supplyAsync(() -> {
            reportClientError.execute(errorReport);
            return created(Json.toJson(errorReport));
        });
    }
}
