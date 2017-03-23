package usecases;

import emailsender.EmailSender;
import models.Application;
import models.User;
import org.jetbrains.annotations.NotNull;
import play.Logger;
import usecases.models.Report;
import usecases.repositories.ApplicationRepository;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.completedFuture;


public class InsertDataPoints {

    private final MetricsDatasource metricsDatasource;
    private final ApplicationRepository applicationRepository;
    private final EmailSender emailSender;

    @Inject
    public InsertDataPoints(MetricsDatasource metricsDatasource, ApplicationRepository applicationRepository, EmailSender emailSender) {
        this.metricsDatasource = metricsDatasource;
        this.applicationRepository = applicationRepository;
        this.emailSender = emailSender;
    }

    public CompletionStage<InsertResult> execute(Report report) {
        if (report.getAppPackage() == null || report.getAppPackage().trim().isEmpty()) {
            Logger.error("InsertDataPoints with AppPackage empty or null");
            Logger.error(report.toString());
            return emptyInsertResult();
        }
        if (report.isDiscardable()) {
            return emptyInsertResult();
        }
        return insertReport(report);
    }

    private CompletionStage<InsertResult> insertReport(Report report) {
        CompletionStage<Application> futureApplication;
        Application application = applicationRepository.getApplicationByApiKeyValueAndAppPackage(report.getApiKey(), report.getAppPackage());
        boolean firstReport = application == null;
        if (firstReport) {
            futureApplication = applicationRepository.create(report.getApiKey(), report.getAppPackage());
            sendFirstReportEmail(futureApplication);
        } else {
            futureApplication = completedFuture(application);
        }
        return writeDataPoints(report, futureApplication, firstReport);
    }

    private void sendFirstReportEmail(CompletionStage<Application> futureApplication) {
        futureApplication.thenCompose(emailSender::sendFirstReportReceived);
    }

    private CompletionStage<InsertResult> writeDataPoints(Report report, CompletionStage<Application> futureApplication, boolean firstReport) {
        return futureApplication.thenCompose((app) -> metricsDatasource.writeDataPoints(report, app));
    }

    @NotNull
    private CompletionStage<InsertResult> emptyInsertResult() {
        return completedFuture(InsertResult.successEmpty());
    }
}
