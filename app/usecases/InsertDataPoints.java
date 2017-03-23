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
        Application application = applicationRepository.getApplicationByApiKeyValueAndAppPackage(report.getApiKey(), report.getAppPackage());
        CompletionStage<Application> futureApplication;
        final boolean firstReport = application == null;
        if (firstReport) {
            futureApplication = applicationRepository.create(report.getApiKey(), report.getAppPackage());
        } else {
            futureApplication = completedFuture(application);
        }
        return futureApplication.thenCompose((app) -> {
            if(firstReport) {
                sendFirstReportReceivedEmail(app);
            }
            return metricsDatasource.writeDataPoints(report, app);
        });
    }

    private void sendFirstReportReceivedEmail(Application app) {
        List<User> users = app.getOrganization().getMembers();
        emailSender.sendFirstReportReceived(users, app);
    }

    @NotNull
    private CompletionStage<InsertResult> emptyInsertResult() {
        return completedFuture(InsertResult.successEmpty());
    }
}
