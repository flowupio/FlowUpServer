package usecases;

import models.Application;
import org.jetbrains.annotations.NotNull;
import play.Logger;
import usecases.models.Report;
import usecases.repositories.ApplicationRepository;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.completedFuture;


public class InsertDataPoints {

    private final MetricsDatasource metricsDatasource;
    private final ApplicationRepository applicationRepository;

    @Inject
    public InsertDataPoints(MetricsDatasource metricsDatasource, ApplicationRepository applicationRepository) {
        this.metricsDatasource = metricsDatasource;
        this.applicationRepository = applicationRepository;
    }

    public CompletionStage<InsertResult> execute(Report report) {
        if (report.getAppPackage() == null || report.getAppPackage().trim().isEmpty()) {
            Logger.error("InsertDataPoints with AppPackage empty or null");
            Logger.error(report.toString());
            return emptyInsertResult();
        }
        if (report.getInDebugMode() && report.getAppInBackground()) {
            return emptyInsertResult();
        }
        Application application = applicationRepository.getApplicationByApiKeyValueAndAppPackage(report.getApiKey(), report.getAppPackage());
        CompletionStage<Application> futureApplication;
        if (application == null) {
            futureApplication = applicationRepository.create(report.getApiKey(), report.getAppPackage());
        } else {
            futureApplication = completedFuture(application);
        }
        return futureApplication.thenCompose((app) -> metricsDatasource.writeDataPoints(report, app));
    }

    @NotNull
    private CompletionStage<InsertResult> emptyInsertResult() {
        return completedFuture(InsertResult.successEmpty());
    }
}
