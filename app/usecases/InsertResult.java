package usecases;

import lombok.Data;

import java.util.List;

@Data
public class InsertResult {
    private final boolean hasErrors;
    private final List<MetricResult> items;

    @Data
    public static class MetricResult {
        private final String name;
        private final long successful;
    }
}
