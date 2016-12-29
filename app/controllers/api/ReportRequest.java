package controllers.api;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import usecases.models.StatisticalValue;

import java.util.Collections;
import java.util.List;

@Data
public class ReportRequest {

    private final String appPackage;
    private final String deviceModel;
    private final String screenDensity;
    private final String screenSize;
    private final String installationUUID;
    private final int numberOfCores;
    private final List<Network> network;
    private final List<Ui> ui;
    private final List<Cpu> cpu;
    private final List<Gpu> gpu;
    private final List<Memory> memory;
    private final List<Disk> disk;

    @NotNull
    public List<Network> getNetwork() {
        return emptyIfNull(network);
    }

    @NotNull
    public List<Ui> getUi() {
        return emptyIfNull(ui);
    }

    @NotNull
    public List<Gpu> getGpu() {
        return emptyIfNull(gpu);
    }

    @NotNull
    public List<Cpu> getCpu() {
        return emptyIfNull(cpu);
    }

    @NotNull
    public List<Memory> getMemory() {
        return emptyIfNull(memory);
    }

    @NotNull
    public List<Disk> getDisk() {
        return emptyIfNull(disk);
    }

    private <T> List<T> emptyIfNull(List<T> list) {
        return list != null ? list : Collections.emptyList();
    }

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
