package usecases;

import java.util.List;
import java.util.concurrent.CompletionStage;

public interface MetricsDatasource {
    CompletionStage<InsertResult> writeDataPoints(List<Metric> metrics);
}
