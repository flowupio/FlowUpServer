package controllers.api;

import models.Platform;
import org.jetbrains.annotations.NotNull;
import play.Logger;
import play.mvc.Http;
import usecases.models.DataPoint;
import usecases.models.Metric;
import usecases.models.Report;

import javax.inject.Inject;
import java.util.List;

public class ReportMapper {

    private final DataPointMapper dataPointMapper;

    @Inject
    public ReportMapper(DataPointMapper dataPointMapper) {
        this.dataPointMapper = dataPointMapper;
    }

    @NotNull
    public Report map(Http.Request request) {
        Http.RequestBody body = request.body();
        ReportRequest reportRequest = body.as(ReportRequest.class);

        List<Metric> metrics = dataPointMapper.mapMetrics(reportRequest);

        String apiKey = request.getHeader(HeaderParsers.X_API_KEY);
        Report.Metadata metadata = new Report.Metadata(isInDebugMode(request, reportRequest), isBackground(reportRequest));
        Platform platform = mapPlatform(metrics);
        return new Report(apiKey, reportRequest.getAppPackage(), metrics, metadata, platform);
    }

    private Boolean isInDebugMode(Http.Request request, ReportRequest reportRequest) {
        String debugHeader = request.getHeader(HeaderParsers.X_DEBUG_MODE);
        if (debugHeader == null) {
            return requestIsInDebugMode(reportRequest);
        }
        return Boolean.valueOf(debugHeader);
    }

    private Boolean requestIsInDebugMode(ReportRequest reportRequest) {
        return reportRequest.getCpu().size() <= 1;
    }

    private Boolean isBackground(ReportRequest reportRequest) {
        return reportRequest.getUi().isEmpty();
    }

    private Platform mapPlatform(List<Metric> metrics) {
        return metrics.stream()
                .anyMatch(this::isAnyMetricFromIOS) ? Platform.IOS : Platform.ANDROID;
    }

    private boolean isAnyMetricFromIOS(Metric metric) {
        return metric.getDataPoints().stream()
                .anyMatch(this::isAnyTagFromIOS);
    }

    private boolean isAnyTagFromIOS(DataPoint dataPoint) {
        return dataPoint.getTags().stream()
                .anyMatch(tagTuple -> {
                    Logger.debug("\t\t[" + tagTuple._1 + ", " + tagTuple._2 + "]");
                    return tagTuple._1.equals(DataPointMapper.IOS_VERSION) && tagTuple._2 != null;
                });
    }
}
