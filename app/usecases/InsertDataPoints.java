package usecases;

import models.Application;

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

        if (!applicationRepository.exist(report.getAppPackage())) {
            Application application = applicationRepository.create(report.getApiKey(), report.getAppPackage());
        }
        return metricsDatasource.writeDataPoints(report);
    }
}
