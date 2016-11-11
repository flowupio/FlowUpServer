package usecases;

import datasources.elasticsearch.ElasticSearchDatasource;
import models.Application;
import usecases.models.StatCard;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class GetMemoryUsage extends GetLineChart {
    @Inject
    public GetMemoryUsage(ElasticSearchDatasource elasticSearchDatasource) {
        super(elasticSearchDatasource);
    }

    public CompletionStage<StatCard> execute(Application application) {
        return super.executeSingleStat(application, "Consumption", "_type:memory_data").thenApply(lineChart -> {
            double average = lineChart.getValues().stream().mapToDouble(a -> a).average().orElseGet(() -> 0.0);
            return new StatCard("Memory Usage", average, "%", lineChart);
        });
    }
}
