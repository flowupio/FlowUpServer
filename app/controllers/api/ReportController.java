package controllers.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import play.Configuration;
import play.Logger;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import sampling.SamplingGroup;
import usecases.*;
import usecases.models.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ReportController extends Controller {
    private final InsertDataPoints insertDataPoints;
    private final DataPointMapper dataPointMapper;
    private final SamplingGroup samplingGroup;
    private final Configuration flowupConf;

    @Inject
    public ReportController(InsertDataPoints insertDataPoints, DataPointMapper dataPointMapper, SamplingGroup samplingGroup, @Named("flowup") Configuration flowupConf) {
        this.insertDataPoints = insertDataPoints;
        this.dataPointMapper = dataPointMapper;
        this.samplingGroup = samplingGroup;
        this.flowupConf = flowupConf;
    }

    @BodyParser.Of(ReportRequestBodyParser.class)
    public CompletionStage<Result> index() {
        Http.RequestBody body = request().body();
        Logger.debug(body.asText());
        ReportRequest reportRequest = body.as(ReportRequest.class);

        String apiKey = request().getHeader(HeaderParsers.X_API_KEY);
        String uuid = request().getHeader(HeaderParsers.X_UUID);
        if (!samplingGroup.isIn(apiKey, uuid)) {
            Integer statusCode = flowupConf.getInt("not_in_sampling_group_status_code", PRECONDITION_FAILED);
            return CompletableFuture.completedFuture(status(statusCode));
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

