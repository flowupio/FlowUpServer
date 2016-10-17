package controllers;

import lombok.Data;
import usecases.StatisticalValue;

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
    private final List<Memory> memory = new ArrayList<>();
    private final List<Disk> disk = new ArrayList<>();

    @Data
    static class Network implements DatapointTags {
        private final long timestamp;
        private final String appVersionName;
        private final String androidOSVersion;
        private final boolean batterySaverOn;
        private final double bytesUploaded;
        private final double bytesDownloaded;
    }

    @Data
    static class Ui implements DatapointTags {
        private final long timestamp;
        private final String appVersionName;
        private final String androidOSVersion;
        private final boolean batterySaverOn;
        private final String screen;
        private final StatisticalValue frameTime;
        private final StatisticalValue framesPerSecond;
        private final StatisticalValue onActivityCreated;
        private final StatisticalValue onActivityStarted;
        private final StatisticalValue onActivityResumed;
        private final StatisticalValue activityVisible;
        private final StatisticalValue onActivityPaused;
        private final StatisticalValue onActivityStopped;
        private final StatisticalValue onActivityDestroyed;
    }

    @Data
    static class Cpu implements DatapointTags, ProcessingUnit {
        private final long timestamp;
        private final String appVersionName;
        private final String androidOSVersion;
        private final boolean batterySaverOn;
        private final double consumption;
    }

    @Data
    static class Gpu implements DatapointTags, ProcessingUnit {
        private final long timestamp;
        private final String appVersionName;
        private final String androidOSVersion;
        private final boolean batterySaverOn;
        private final double consumption;
    }


    @Data
    public static class Disk implements DatapointTags {
        private final long timestamp;
        private final String appVersionName;
        private final String androidOSVersion;
        private final boolean batterySaverOn;
        private final long internalStorageWrittenBytes;
        private final long sharedPreferencesWrittenBytes;
    }

    @Data
    public static class Memory implements DatapointTags {
        private final long timestamp;
        private final String appVersionName;
        private final String androidOSVersion;
        private final boolean batterySaverOn;
        private final double consumption;
        private final long bytesAllocated;
    }

}
