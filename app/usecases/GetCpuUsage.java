package usecases;

import datasources.elasticsearch.ElasticSearchDatasource;
import models.Application;
import usecases.models.StatCard;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class GetCpuUsage extends GetLineChart {
    @Inject
    public GetCpuUsage(ElasticSearchDatasource elasticSearchDatasource) {
        super(elasticSearchDatasource);
    }

    public CompletionStage<StatCard> execute(Application application) {
        return super.executeSingleStat(application, "Consumption", " _type:cpu_data").thenApply(lineChart -> {
            double average = lineChart.getValues().stream().mapToDouble(a -> a).average().orElseGet(() -> 0.0);
            return new StatCard("CPU Usage", average, "%", lineChart);
        });
    }
}
