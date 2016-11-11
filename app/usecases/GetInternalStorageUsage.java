package usecases;

import datasources.elasticsearch.ElasticSearchDatasource;
import models.Application;
import usecases.models.StatCard;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class GetInternalStorageUsage extends GetLineChart {
    @Inject
    public GetInternalStorageUsage(ElasticSearchDatasource elasticSearchDatasource) {
        super(elasticSearchDatasource);
    }

    public CompletionStage<StatCard> execute(Application application) {
        return super.executeSingleStat(application, "InternalStorageWrittenBytes").thenApply(lineChart -> {
            double average = lineChart.getValues().stream().mapToDouble(a -> a).average().orElseGet(() -> 0.0);
            return new StatCard("Internal Storage", average, "Kb", lineChart);
        });
    }
}
