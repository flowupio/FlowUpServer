package usecases;

import models.Application;
import usecases.models.LineChart;
import usecases.models.Report;

import java.util.concurrent.CompletionStage;

public interface MetricsDatasource {
    CompletionStage<InsertResult> writeDataPoints(Report report, Application application);

    CompletionStage<LineChart> singleStat(SingleStatQuery singleStatQuery);
}
