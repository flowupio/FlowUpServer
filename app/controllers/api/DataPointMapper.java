package controllers.api;

import org.jetbrains.annotations.NotNull;
import play.Logger;
import play.libs.F;
import usecases.models.DataPoint;
import usecases.models.Metric;
import usecases.models.StatisticalValue;
import usecases.models.Value;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DataPointMapper {

    private static final String BYTES_UPLOADED = "BytesUploaded";
    private static final String BYTES_DOWNLOADED = "BytesDownloaded";
    private static final String APP_PACKAGE = "AppPackage";
    private static final String DEVICE_MODEL = "DeviceModel";
    private static final String SCREEN_DENSITY = "ScreenDensity";
    private static final String SCREEN_SIZE = "ScreenSize";
    private static final String INSTALLATION_UUID = "InstallationUUID";
    private static final String NUMBER_OF_CORES = "NumberOfCores";
    private static final String VERSION_NAME = "VersionName";
    private static final String ANDROID_OS_VERSION = "AndroidOSVersion";
    static final String IOS_VERSION = "IOSVersion";
    private static final String BATTERY_SAVER_ON = "BatterySaverOn";
    private static final String SCREEN_NAME = "ScreenName";
    private static final String FRAMES_PER_SECOND = "FramesPerSecond";
    private static final String FRAME_TIME = "FrameTime";
    private static final String CONSUMPTION = "Consumption";
    private static final String INTERNAL_STORAGE_WRITTEN_BYTES = "InternalStorageWrittenBytes";
    private static final String SHARED_PREFERENCES_WRITTEN_BYTES = "SharedPreferencesWrittenBytes";
    private static final String USER_DEFAULTS_WRITTEN_BYTES = "UserDefaultsWrittenBytes";
    private static final String BYTES_ALLOCATED = "BytesAllocated";
    private static final String ON_ACTIVITY_CREATED_TIME = "OnActivityCreatedTime";
    private static final String ON_ACTIVITY_STARTED_TIME = "OnActivityStartedTime";
    private static final String ON_ACTIVITY_RESUMED_TIME = "OnActivityResumedTime";
    private static final String ACTIVITY_VISIBLE_TIME = "ActivityTime";
    private static final String ON_ACTIVITY_PAUSED_TIME = "OnActivityPausedTime";
    private static final String ON_ACTIVITY_STOPPED_TIME = "OnActivityStoppedTime";
    private static final String ON_ACTIVITY_DESTROYED_TIME = "OnActivityDestroyedTime";
    private static final AndroidAPI MIN_CPU_API_SUPPORTED = new AndroidAPI(19);
    private static final long MIN_FRAME_TIME_ALLOWED = TimeUnit.MILLISECONDS.toNanos(16);

    @NotNull
    public List<Metric> mapMetrics(ReportRequest reportRequest) {
        List<Metric> metrics = new ArrayList<>();
        metrics.add(new Metric("network_data", this.mapNetwork(reportRequest)));
        metrics.add(new Metric("ui_data", this.mapUi(reportRequest)));
        metrics.add(new Metric("cpu_data", this.mapCpu(reportRequest)));
        metrics.add(new Metric("gpu_data", this.mapGpu(reportRequest)));
        metrics.add(new Metric("memory_data", this.mapMemory(reportRequest)));
        metrics.add(new Metric("disk_data", this.mapDisk(reportRequest)));
        return metrics;
    }

    public List<DataPoint> mapNetwork(ReportRequest reportRequest) {
        List<DataPoint> dataPoints = new ArrayList<>();

        reportRequest.getNetwork().forEach((network) -> {
            List<F.Tuple<String, Value>> measurements = new ArrayList<>();
            measurements.add(new F.Tuple<>(BYTES_UPLOADED, Value.toBasicValue(network.getBytesUploaded())));
            measurements.add(new F.Tuple<>(BYTES_DOWNLOADED, Value.toBasicValue(network.getBytesDownloaded())));

            List<F.Tuple<String, String>> tags = new ArrayList<>();
            addReportLevelTags(tags, reportRequest);
            addDataPointLevelTags(tags, network);

            dataPoints.add(new DataPoint(new Date(network.getTimestamp()), measurements, tags));
        });

        return dataPoints;
    }

    public List<DataPoint> mapUi(ReportRequest reportRequest) {
        List<DataPoint> dataPoints = new ArrayList<>();

        reportRequest.getUi().forEach((ui) -> {
            List<F.Tuple<String, Value>> measurements = new ArrayList<>();
            StatisticalValue frameTime = ui.getFrameTime();
            if (frameTime.getMean() >= MIN_FRAME_TIME_ALLOWED) {
                measurements.add(new F.Tuple<>(FRAME_TIME, frameTime));
                measurements.add(new F.Tuple<>(FRAMES_PER_SECOND, computeFramesPerSecond(frameTime)));
            } else {
                Logger.error("Invalid frame time metric detected " + reportRequest);
            }
            measurements.add(new F.Tuple<>(ON_ACTIVITY_CREATED_TIME, ui.getOnActivityCreatedTime()));
            measurements.add(new F.Tuple<>(ON_ACTIVITY_STARTED_TIME, ui.getOnActivityStartedTime()));
            measurements.add(new F.Tuple<>(ON_ACTIVITY_RESUMED_TIME, ui.getOnActivityResumedTime()));
            measurements.add(new F.Tuple<>(ACTIVITY_VISIBLE_TIME, ui.getActivityVisibleTime()));
            measurements.add(new F.Tuple<>(ON_ACTIVITY_PAUSED_TIME, ui.getOnActivityPausedTime()));
            measurements.add(new F.Tuple<>(ON_ACTIVITY_STOPPED_TIME, ui.getOnActivityStoppedTime()));
            measurements.add(new F.Tuple<>(ON_ACTIVITY_DESTROYED_TIME, ui.getOnActivityDestroyedTime()));
            List<F.Tuple<String, String>> tags = new ArrayList<>();
            addReportLevelTags(tags, reportRequest);
            addDataPointLevelTags(tags, ui);
            tags.add(new F.Tuple<>(SCREEN_NAME, ui.getScreen()));

            dataPoints.add(new DataPoint(new Date(ui.getTimestamp()), measurements, tags));
        });

        return dataPoints;
    }

    private StatisticalValue computeFramesPerSecond(StatisticalValue frameTime) {
        if (frameTime == null) {
            return null;
        }
        return new StatisticalValue(
                frameTimeToFramePerSecond(frameTime.getMean()),
                frameTimeToFramePerSecond(frameTime.getP10()),
                frameTimeToFramePerSecond(frameTime.getP90())
        );
    }

    private double frameTimeToFramePerSecond(double value) {
        if (value == 0.0) return 1000000000.0;
        return (1.0 / value) * 1000000000.0;
    }

    public List<DataPoint> mapCpu(ReportRequest reportRequest) {
        List<DataPoint> dataPoints = new ArrayList<>();

        reportRequest.getCpu().forEach((cpu) -> {
            mapProcessingUnit(reportRequest, dataPoints, cpu, MIN_CPU_API_SUPPORTED);
        });

        return dataPoints;
    }

    public List<DataPoint> mapGpu(ReportRequest reportRequest) {
        List<DataPoint> dataPoints = new ArrayList<>();

        reportRequest.getGpu().forEach((gpu) -> {
            mapProcessingUnit(reportRequest, dataPoints, gpu);
        });

        return dataPoints;
    }

    public List<DataPoint> mapMemory(ReportRequest reportRequest) {
        List<DataPoint> dataPoints = new ArrayList<>();

        reportRequest.getMemory().forEach((memory) -> {
            mapMemory(reportRequest, dataPoints, memory);
        });

        return dataPoints;
    }

    public List<DataPoint> mapDisk(ReportRequest reportRequest) {
        List<DataPoint> dataPoints = new ArrayList<>();

        reportRequest.getDisk().forEach((disk) -> {
            mapDisk(reportRequest, dataPoints, disk);
        });

        return dataPoints;
    }

    private void addDataPointLevelTags(List<F.Tuple<String, String>> tags, DatapointTags datapointTags) {
        tags.add(new F.Tuple<>(VERSION_NAME, datapointTags.getAppVersionName()));
        tags.add(new F.Tuple<>(ANDROID_OS_VERSION, datapointTags.getAndroidOSVersion()));
        tags.add(new F.Tuple<>(IOS_VERSION, datapointTags.getIOSVersion()));
        tags.add(new F.Tuple<>(BATTERY_SAVER_ON, Boolean.toString(datapointTags.isBatterySaverOn())));
    }

    private void addReportLevelTags(List<F.Tuple<String, String>> tags, ReportRequest reportRequest) {
        tags.add(new F.Tuple<>(APP_PACKAGE, reportRequest.getAppPackage()));
        tags.add(new F.Tuple<>(DEVICE_MODEL, reportRequest.getDeviceModel()));
        tags.add(new F.Tuple<>(SCREEN_DENSITY, reportRequest.getScreenDensity()));
        tags.add(new F.Tuple<>(SCREEN_SIZE, reportRequest.getScreenSize()));
        tags.add(new F.Tuple<>(INSTALLATION_UUID, reportRequest.getInstallationUUID()));
        tags.add(new F.Tuple<>(NUMBER_OF_CORES, Integer.toString(reportRequest.getNumberOfCores())));
    }

    private void mapProcessingUnit(ReportRequest reportRequest, List<DataPoint> dataPoints, ProcessingUnit processingUnit) {
        mapProcessingUnit(reportRequest, dataPoints, processingUnit, null);
    }

    private void mapProcessingUnit(ReportRequest reportRequest, List<DataPoint> dataPoints, ProcessingUnit processingUnit, AndroidAPI minAPISupported) {
        /* We have noticed a bug in the Android SDK and the client is reporting CPU consumption as 0 % always.
         * Based on this bug we have decided to don't store some data points if the host API is not supported.
         * In the case of the CPU metric, the min API supported is 19.
         */
        if (processingUnit.isAndroid()) {
            AndroidAPI metricAndroidAPI = AndroidAPI.fromString(processingUnit.getAndroidOSVersion());
            if (minAPISupported != null && metricAndroidAPI.compareTo(minAPISupported) > 0) {
                return;
            }
        }
        List<F.Tuple<String, Value>> measurements = new ArrayList<>();
        measurements.add(new F.Tuple<>(CONSUMPTION, Value.toBasicValue(processingUnit.getConsumption())));

        List<F.Tuple<String, String>> tags = new ArrayList<>();
        addReportLevelTags(tags, reportRequest);
        addDataPointLevelTags(tags, processingUnit);

        dataPoints.add(new DataPoint(new Date(processingUnit.getTimestamp()), measurements, tags));
    }

    private void mapMemory(ReportRequest reportRequest, List<DataPoint> dataPoints, ReportRequest.Memory memory) {
        List<F.Tuple<String, Value>> measurements = new ArrayList<>();
        measurements.add(new F.Tuple<>(CONSUMPTION, Value.toBasicValue(memory.getConsumption())));
        measurements.add(new F.Tuple<>(BYTES_ALLOCATED, Value.toBasicValue(memory.getBytesAllocated())));
        List<F.Tuple<String, String>> tags = new ArrayList<>();
        addReportLevelTags(tags, reportRequest);
        addDataPointLevelTags(tags, memory);

        dataPoints.add(new DataPoint(new Date(memory.getTimestamp()), measurements, tags));
    }

    private void mapDisk(ReportRequest reportRequest, List<DataPoint> dataPoints, ReportRequest.Disk disk) {
        List<F.Tuple<String, Value>> measurements = new ArrayList<>();
        measurements.add(new F.Tuple<>(INTERNAL_STORAGE_WRITTEN_BYTES, Value.toBasicValue(disk.getInternalStorageWrittenBytes())));
        measurements.add(new F.Tuple<>(SHARED_PREFERENCES_WRITTEN_BYTES, Value.toBasicValue(disk.getSharedPreferencesWrittenBytes())));
        measurements.add(new F.Tuple<>(USER_DEFAULTS_WRITTEN_BYTES, Value.toBasicValue(disk.getUserDefaultsWrittenBytes())));
        List<F.Tuple<String, String>> tags = new ArrayList<>();
        addReportLevelTags(tags, reportRequest);
        addDataPointLevelTags(tags, disk);

        dataPoints.add(new DataPoint(new Date(disk.getTimestamp()), measurements, tags));
    }
}
