package usecases;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.concurrent.CompletionStage;

public interface MetricsDatasource {
    CompletionStage<JsonNode> writeDataPoints(List<Metric> metrics);
}
