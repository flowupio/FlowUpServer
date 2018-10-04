package controllers.api;

import com.fasterxml.jackson.databind.JsonNode;
import models.Version;
import play.Configuration;
import play.Logger;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import sampling.ApiKeyPrivilege;
import usecases.InsertDataPoints;
import usecases.models.Report;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ReportController extends Controller {
    private final InsertDataPoints insertDataPoints;
    private final ApiKeyPrivilege apiKeyPrivilege;
    private final Configuration flowupConf;
    private final ReportMapper reportMapper;

    @Inject
    public ReportController(InsertDataPoints insertDataPoints, ReportMapper reportMapper, ApiKeyPrivilege apiKeyPrivilege, @Named("flowup") Configuration flowupConf) {
        this.insertDataPoints = insertDataPoints;
        this.reportMapper = reportMapper;
        this.apiKeyPrivilege = apiKeyPrivilege;
        this.flowupConf = flowupConf;
    }

    @BodyParser.Of(ReportRequestBodyParser.class)
    public CompletionStage<Result> index() {
        String body = request().body().asText();
        Logger.debug(body);

        String apiKey = request().getHeader(HeaderParsers.X_API_KEY);
        String uuid = request().getHeader(HeaderParsers.X_UUID);
        String userAgent = request().getHeader(HeaderParsers.USER_AGENT);
        if (!apiKeyPrivilege.isAllowed(apiKey, uuid, Version.fromString(userAgent))) {
            Integer statusCode = flowupConf.getInt("not_in_sampling_group_status_code", FORBIDDEN);
            return CompletableFuture.completedFuture(status(statusCode));
        }

        Report report = reportMapper.map(request());

        return insertDataPoints.execute(report).thenApply(result -> {
                    ReportResponse reportResponse = new ReportResponse("Metrics Inserted", result);
                    JsonNode content = Json.toJson(reportResponse);

                    if (result.isError()) {
                        return internalServerError(content);
                    } else if (result.isHasFailures()) {
                        Logger.error(body);
                        return status(SERVICE_UNAVAILABLE, content);
                    } else {
                        return created(content);
                    }
                }
        );
    }

}

