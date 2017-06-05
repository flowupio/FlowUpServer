package controllers.api;

import models.Platform;
import org.jetbrains.annotations.NotNull;
import play.mvc.Http;
import usecases.models.DataPoint;
import usecases.models.Metric;
import usecases.models.Report;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

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
                .findFirst()
                .map(this::getPlatformFromMetric)
                .orElse(Platform.ANDROID);
    }

    private Platform getPlatformFromMetric(Metric metric) {
        return metric.getDataPoints().stream()
                .findFirst()
                .map(this::getPlatformFromDataPoint)
                .orElse(Platform.ANDROID);
    }

    private Platform getPlatformFromDataPoint(DataPoint dataPoint) {
        boolean hasValidIOSTag = dataPoint.getTags().stream()
                .filter(tagTuple -> tagTuple._1.equals(DataPointMapper.IOS_VERSION) && tagTuple._2 != null)
                .collect(Collectors.toList())
                .isEmpty();

        return hasValidIOSTag ? Platform.IOS : Platform.ANDROID;
    }
}
