package usecases;

import models.Application;
import play.Logger;
import usecases.models.Report;
import usecases.repositories.ApplicationRepository;

import javax.inject.Inject;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;


public class InsertDataPoints {

    private final MetricsDatasource metricsDatasource;
    private final ApplicationRepository applicationRepository;

    @Inject
    public InsertDataPoints(MetricsDatasource metricsDatasource, ApplicationRepository applicationRepository) {
        this.metricsDatasource = metricsDatasource;
        this.applicationRepository = applicationRepository;
    }

    public CompletionStage<InsertResult> execute(Report report) {
        if (report.getAppPackage() == null || report.getAppPackage().isEmpty()) {
            Logger.error("InsertDataPoints with AppPackage empty or null");
            Logger.error(report.toString());
            return CompletableFuture.completedFuture(new InsertResult(false, false, Collections.emptyList()));
        }
        if (report.getInDebugMode() && report.getAppInBackground()) {
            return CompletableFuture.completedFuture(new InsertResult(false, false, Collections.emptyList()));
        }
        Application application = applicationRepository.getApplicationByApiKeyValueAndAppPackage(report.getApiKey(), report.getAppPackage());
        if (application == null) {
            return applicationRepository.create(report.getApiKey(), report.getAppPackage()).thenCompose(application1 -> {
                return metricsDatasource.writeDataPoints(report, application1);
            });
        }
        return metricsDatasource.writeDataPoints(report, application);
    }
}
