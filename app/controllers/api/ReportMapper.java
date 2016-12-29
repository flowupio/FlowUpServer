package controllers.api;

import org.jetbrains.annotations.NotNull;
import play.mvc.Http;
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
        return new Report(apiKey, reportRequest.getAppPackage(), metrics, metadata);
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
}
