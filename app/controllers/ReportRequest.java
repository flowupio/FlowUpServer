package controllers;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import usecases.StatisticalValue;

import java.util.Collections;
import java.util.List;

@Data
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class ReportRequest {
    private final String appPackage;
    private final String deviceModel;
    private final String screenDensity;
    private final String screenSize;
    private final String installationUUID;
    private final int numberOfCores;
    private final List<Network> network = Collections.EMPTY_LIST;
    private final List<Ui> ui = Collections.EMPTY_LIST;
    private final List<Cpu> cpu = Collections.EMPTY_LIST;
    private final List<Gpu> gpu = Collections.EMPTY_LIST;
    private final List<Memory> memory = Collections.EMPTY_LIST;
    private final List<Disk> disk = Collections.EMPTY_LIST;

    @Data
    public static class Network implements DatapointTags {
        private final long timestamp;
        private final String appVersionName;
        private final String androidOSVersion;
        private final boolean batterySaverOn;
        private final double bytesUploaded;
        private final double bytesDownloaded;
    }

    @Data
    public static class Ui implements DatapointTags {
        private final long timestamp;
        private final String appVersionName;
        private final String androidOSVersion;
        private final boolean batterySaverOn;
        private final String screen;
        private final StatisticalValue frameTime;
        private final StatisticalValue framesPerSecond;
        private final StatisticalValue onActivityCreatedTime;
        private final StatisticalValue onActivityStartedTime;
        private final StatisticalValue onActivityResumedTime;
        private final StatisticalValue activityVisibleTime;
        private final StatisticalValue onActivityPausedTime;
        private final StatisticalValue onActivityStoppedTime;
        private final StatisticalValue onActivityDestroyedTime;
    }

    @Data
    public static class Cpu implements DatapointTags, ProcessingUnit {
        private final long timestamp;
        private final String appVersionName;
        private final String androidOSVersion;
        private final boolean batterySaverOn;
        private final double consumption;
    }

    @Data
    public static class Gpu implements DatapointTags, ProcessingUnit {
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
