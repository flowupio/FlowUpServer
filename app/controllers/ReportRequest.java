package controllers;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
class ReportRequest {
    private final String appPackage;
    private final String deviceModel;
    private final String screenDensity;
    private final String screenSize;
    private final String installationUUID;
    private final int numberOfCores;
    private final List<Network> network = new ArrayList<>();
    private final List<Ui> ui = new ArrayList<>();
    private final List<Cpu> cpu = new ArrayList<>();
    private final List<Gpu> gpu = new ArrayList<>();

    @Data
    static class Network implements DatapointTags {
        private final long timestamp;
        private final String versionName;
        private final String androidOSVersion;
        private final boolean batterySaverOn;
        private final double bytesUploaded;
        private final double bytesDownloaded;
    }

    @Data
    static class Ui implements DatapointTags {
        private final long timestamp;
        private final String versionName;
        private final String androidOSVersion;
        private final boolean batterySaverOn;
        private final String screenName;
        private final StatisticalValue frameTime;
        private final StatisticalValue framesPerSecond;
    }

    @Data
    static class Cpu implements DatapointTags {
        private final long timestamp;
        private final String versionName;
        private final String androidOSVersion;
        private final boolean batterySaverOn;
        private final double consumption;
    }

    @Data
    static class Gpu implements DatapointTags {
        private final long timestamp;
        private final String versionName;
        private final String androidOSVersion;
        private final boolean batterySaverOn;
        private final double consumption;
    }

    @Data
    static class StatisticalValue {
        private final long count;
        private final double min;
        private final double max;
        private final double mean;
        private final double standardDev;
        private final double median;
        private final double p5;
        private final double p10;
        private final double p15;
        private final double p20;
        private final double p25;
        private final double p30;
        private final double p40;
        private final double p50;
        private final double p60;
        private final double p70;
        private final double p75;
        private final double p80;
        private final double p85;
        private final double p90;
        private final double p95;
        private final double p98;
        private final double p99;
    }
}
