package controllers.api;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.*;
import org.jetbrains.annotations.NotNull;
import play.Logger;
import play.libs.F;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import sampling.SamplingGroup;
import usecases.*;
import usecases.models.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ReportController extends Controller {
    @Inject
    InsertDataPoints insertDataPoints;
    @Inject
    DataPointMapper dataPointMapper;
    @Inject
    SamplingGroup samplingGroup;

    @BodyParser.Of(ReportRequestBodyParser.class)
    public CompletionStage<Result> index() {
        Http.RequestBody body = request().body();
        Logger.debug(body.asText());
        ReportRequest reportRequest = body.as(ReportRequest.class);

        String apiKey = request().getHeader(HeaderParsers.X_API_KEY);
        String uuid = request().getHeader(HeaderParsers.X_UUID);
        if (!samplingGroup.isIn(apiKey, uuid)) {
            return CompletableFuture.completedFuture(status(PRECONDITION_FAILED));
        }

        List<Metric> metrics = dataPointMapper.mapMetrics(reportRequest);

        Report report = new Report(apiKey, reportRequest.getAppPackage(), metrics);

        return insertDataPoints.execute(report).thenApply(result -> {
                    ReportResponse reportResponse = new ReportResponse("Metrics Inserted", result);
                    JsonNode content = Json.toJson(reportResponse);

                    if (result.isError()) {
                        return internalServerError(content);
                    } else if (result.isHasFailures()) {
                        return status(SERVICE_UNAVAILABLE, content);
                    } else {
                        return created(content);
                    }
                }
        );
    }
}

