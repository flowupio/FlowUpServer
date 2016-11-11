package usecases;

import datasources.elasticsearch.ElasticSearchDatasource;
import models.Application;
import usecases.models.StatCard;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class GetFramePerSecond {
    private final ElasticSearchDatasource elasticSearchDatasource;

    @Inject
    public GetFramePerSecond(ElasticSearchDatasource elasticSearchDatasource) {
        this.elasticSearchDatasource = elasticSearchDatasource;
    }

    public CompletionStage<StatCard> execute(Application application) {
        return elasticSearchDatasource.singleStat(application).thenApply(lineChart -> {
            double average = lineChart.getValues().stream().mapToDouble(a -> a).average().orElseGet(() -> 0.0);
            return new StatCard("Frame Per Second", average, null, lineChart);
        });
    }
}
