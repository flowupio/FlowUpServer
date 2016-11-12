package usecases;

import datasources.elasticsearch.ElasticSearchDatasource;
import models.Application;
import org.jetbrains.annotations.NotNull;
import usecases.models.LineChart;
import usecases.models.StatCard;
import usecases.models.Threshold;
import usecases.models.Unit;

import javax.inject.Inject;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.OptionalDouble;
import java.util.concurrent.CompletionStage;

abstract class GetLineChart {
    private final ElasticSearchDatasource elasticSearchDatasource;

    @Inject
    GetLineChart(ElasticSearchDatasource elasticSearchDatasource) {
        this.elasticSearchDatasource = elasticSearchDatasource;
    }

    CompletionStage<StatCard> execute(Application application, String field, String description, Unit unit) {
        Instant now = Instant.now();
        return elasticSearchDatasource.singleStat(application, field, now.minus(6, ChronoUnit.HOURS), now).thenApply(lineChart -> formatStatCard(description, unit, lineChart));
    }

    CompletionStage<StatCard> execute(Application application, String field, String queryStringValue, String description, Unit unit) {
        Instant now = Instant.now();
        return elasticSearchDatasource.singleStat(application, field, queryStringValue, now.minus(6, ChronoUnit.HOURS), now).thenApply(lineChart -> formatStatCard(description, unit, lineChart));
    }

    @NotNull
    private StatCard formatStatCard(String description, Unit unit, LineChart lineChart) {
        OptionalDouble optionalAverage = lineChart.getValues().stream().filter(value -> value != null).mapToDouble(value -> value).average();
        Double average = optionalAverage.isPresent() ? optionalAverage.getAsDouble(): null;
        Threshold threshold = getThreshold(average);
        return new StatCard(description, average, unit, lineChart, threshold);
    }

    abstract Threshold getThreshold(Double average);
}
