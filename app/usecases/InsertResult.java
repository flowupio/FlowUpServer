package usecases;

import lombok.Data;

import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;

@Data
public class InsertResult {
    private final boolean isError;
    private final boolean hasFailures;
    private final List<MetricResult> items;

    @Data
    public static class MetricResult {
        private final String name;
        private final long successful;
    }

    public static InsertResult successEmpty() {
        return new InsertResult(false, false, emptyList());
    }
}
