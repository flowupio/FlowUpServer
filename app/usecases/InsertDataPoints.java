package usecases;

import models.Application;
import usecases.models.Report;
import usecases.repositories.ApplicationRepository;

import javax.inject.Inject;
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

        Application application = applicationRepository.getApplicationByApiKeyValueAndAppPackage(report.getApiKey(), report.getAppPackage());
        if (application == null) {
            return applicationRepository.create(report.getApiKey(), report.getAppPackage()).thenCompose(application1 -> {
                return metricsDatasource.writeDataPoints(report, application1);
            });
        }
        return metricsDatasource.writeDataPoints(report, application);
    }
}
