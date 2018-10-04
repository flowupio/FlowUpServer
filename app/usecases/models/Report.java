package usecases.models;

import lombok.Data;
import models.Platform;

import java.util.List;

@Data
public class Report {
    private final String apiKey;
    private final String appPackage;
    private final List<Metric> metrics;
    private final Metadata metadata;
    private final Platform platform;

    @Data
    public static class Metadata {
        private final Boolean inDebugMode;
        private final Boolean appInBackground;
    }

    public Boolean isDiscardable() {
        return metadata.appInBackground && metadata.inDebugMode;
    }
}
