package usecases;

import com.google.inject.Inject;
import models.Application;
import org.jetbrains.annotations.NotNull;
import play.libs.F;
import usecases.models.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

abstract class GetLineChart {
    private static final String VERSION_NAME = "VersionName";
    private final MetricsDatasource metricsDatasource;

    @Inject
    protected GetLineChart(MetricsDatasource metricsDatasource) {
        this.metricsDatasource = metricsDatasource;
    }

    CompletionStage<KeyStatCard> execute(Application application, String field, String description, Unit unit) {
        F.Tuple<Instant, Instant> bounds = getInstantBounds();
        return metricsDatasource.singleStat(new SingleStatQuery(application, field, bounds))
                .thenApply(lineChart -> formatStatCard(description, unit, lineChart))
                .thenCompose(statCard -> getKeyStatCardCompletionStage(application, field, unit, bounds, statCard, description));
    }

    CompletionStage<KeyStatCard> execute(Application application, String field, String queryStringValue, String description, Unit unit) {
        F.Tuple<Instant, Instant> bounds = getInstantBounds();
        SingleStatQuery singleStatQuery = new SingleStatQuery(application, field, bounds);
        singleStatQuery.setQueryStringValue(queryStringValue);
        return metricsDatasource.singleStat(singleStatQuery)
                .thenApply(lineChart -> formatStatCard(description, unit, lineChart))
                .thenCompose(statCard -> getKeyStatCardCompletionStage(application, field, unit, bounds, statCard, description));
    }

    @NotNull
    private F.Tuple<Instant, Instant> getInstantBounds() {
        Instant now = Instant.now();
        Instant nowMinus6Hours = now.minus(24, ChronoUnit.HOURS);
        return new F.Tuple<>(nowMinus6Hours, now);
    }

    private CompletionStage<KeyStatCard> getKeyStatCardCompletionStage(Application application, String field, Unit unit, F.Tuple<Instant, Instant> bounds, StatCard statCard, String description) {
        KeyStatCard keyStatCard = new KeyStatCard(statCard, description);
        if (statCard.getThreshold().isWarningOrWorse()) {
            return metricsDatasource.statGroupBy(new SingleStatQuery(application, field, bounds), VERSION_NAME).thenApply(lineCharts -> {
                List<StatCard> details = lineCharts.stream().map(lineChart -> formatStatCard(lineChart.getName(), unit, lineChart)).collect(Collectors.toList());
                keyStatCard.setDetails(details);
                return keyStatCard;
            });
        }
        return CompletableFuture.completedFuture(keyStatCard);
    }

    @NotNull
    private StatCard formatStatCard(String description, Unit unit, LineChart lineChart) {
        OptionalDouble optionalAverage = lineChart.getValues().stream().filter(Objects::nonNull).mapToDouble(value -> value).average();
        Double average = optionalAverage.isPresent() ? optionalAverage.getAsDouble() : null;
        Threshold threshold = getThreshold(average);
        return new StatCard(description, average, unit, lineChart, threshold);
    }

    abstract Threshold getThreshold(Double average);
}