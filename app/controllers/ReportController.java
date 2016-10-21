package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.F;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import usecases.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class ReportController extends Controller {
    @Inject
    InsertDataPoints insertDataPoints;

    @Inject
    DataPointMapper dataPointMapper;

    @BodyParser.Of(ReportRequestBodyParser.class)
    public CompletionStage<Result> index() {
        Http.RequestBody body = request().body();
        ReportRequest reportRequest = body.as(ReportRequest.class);

        // Hardcoded for now, when organization management is ready we will use the UUID of the Org.
        // Ticket https://github.com/Karumi/FlowUpServer/issues/64
        String organizationIdentifier = "3e02e6b9-3a33-4113-ae78-7d37f11ca3bf";

        List<Metric> metrics = new ArrayList<>();
        metrics.add(new Metric("network_data", dataPointMapper.mapNetwork(reportRequest)));
        metrics.add(new Metric("ui_data", dataPointMapper.mapUi(reportRequest)));
        metrics.add(new Metric("cpu_data", dataPointMapper.mapCpu(reportRequest)));
        metrics.add(new Metric("gpu_data", dataPointMapper.mapGpu(reportRequest)));
        metrics.add(new Metric("memory_data", dataPointMapper.mapMemory(reportRequest)));
        metrics.add(new Metric("disk_data", dataPointMapper.mapDisk(reportRequest)));

        Report report = new Report(organizationIdentifier, reportRequest.getAppPackage(), metrics);

        return insertDataPoints.execute(report).thenApply(result -> {
                    ReportResponse reportResponse = new ReportResponse("Metrics Inserted", result);
                    JsonNode content = Json.toJson(reportResponse);

                    if (result.isError()) {
                        return internalServerError(content);
                    } else if (result.isHasFailures()) {
                        return status(SERVICE_UNAVAILABLE, content);
                    } else {
                        return created(content);
                    }
                }
        );
    }
}

class DataPointMapper {

    static final String BYTES_UPLOADED = "BytesUploaded";
    static final String BYTES_DOWNLOADED = "BytesDownloaded";
    static final String APP_PACKAGE = "AppPackage";
    static final String DEVICE_MODEL = "DeviceModel";
    static final String SCREEN_DENSITY = "ScreenDensity";
    static final String SCREEN_SIZE = "ScreenSize";
    static final String INSTALLATION_UUID = "InstallationUUID";
    static final String NUMBER_OF_CORES = "NumberOfCores";
    static final String VERSION_NAME = "VersionName";
    static final String ANDROID_OS_VERSION = "AndroidOSVersion";
    static final String BATTERY_SAVER_ON = "BatterySaverOn";
    static final String SCREEN_NAME = "ScreenName";
    static final String FRAMES_PER_SECOND = "FramesPerSecond";
    static final String FRAME_TIME = "FrameTime";
    static final String CONSUMPTION = "Consumption";
    static final String INTERNAL_STORAGE_WRITTEN_BYTES = "InternalStorageWrittenBytes";
    static final String SHARED_PREFERENCES_WRITTEN_BYTES = "SharedPreferencesWrittenBytes";
    static final String BYTES_ALLOCATED = "BytesAllocated";
    static final String ON_ACTIVITY_CREATED_TIME = "OnActivityCreatedTime";
    static final String ON_ACTIVITY_STARTED_TIME = "OnActivityStartedTime";
    static final String ON_ACTIVITY_RESUMED_TIME = "OnActivityResumedTime";
    static final String ACTIVITY_VISIBLE_TIME = "ActivityTime";
    static final String ON_ACTIVITY_PAUSED_TIME = "OnActivityPausedTime";
    static final String ON_ACTIVITY_STOPPED_TIME = "OnActivityStoppedTime";
    static final String ON_ACTIVITY_DESTROYED_TIME = "OnActivityDestroyedTime";

    List<DataPoint> mapNetwork(ReportRequest reportRequest) {
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

    List<DataPoint> mapUi(ReportRequest reportRequest) {
        List<DataPoint> dataPoints = new ArrayList<>();

        reportRequest.getUi().forEach((ui) -> {
            List<F.Tuple<String, Value>> measurements = new ArrayList<>();
            measurements.add(new F.Tuple<>(FRAME_TIME, ui.getFrameTime()));
            measurements.add(new F.Tuple<>(FRAMES_PER_SECOND, ui.getFramesPerSecond()));
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

    List<DataPoint> mapCpu(ReportRequest reportRequest) {
        List<DataPoint> dataPoints = new ArrayList<>();

        reportRequest.getCpu().forEach((cpu) -> {
            mapProcessingUnit(reportRequest, dataPoints, cpu);
        });

        return dataPoints;
    }

    List<DataPoint> mapGpu(ReportRequest reportRequest) {
        List<DataPoint> dataPoints = new ArrayList<>();

        reportRequest.getGpu().forEach((gpu) -> {
            mapProcessingUnit(reportRequest, dataPoints, gpu);
        });

        return dataPoints;
    }

    List<DataPoint> mapMemory(ReportRequest reportRequest) {
        List<DataPoint> dataPoints = new ArrayList<>();

        reportRequest.getMemory().forEach((memory) -> {
            mapMemory(reportRequest, dataPoints, memory);
        });

        return dataPoints;
    }

    List<DataPoint> mapDisk(ReportRequest reportRequest) {
        List<DataPoint> dataPoints = new ArrayList<>();

        reportRequest.getDisk().forEach((disk) -> {
            mapDisk(reportRequest, dataPoints, disk);
        });

        return dataPoints;
    }

    private void addDataPointLevelTags(List<F.Tuple<String, String>> tags, DatapointTags datapointTags) {
        tags.add(new F.Tuple<>(VERSION_NAME, datapointTags.getAppVersionName()));
        tags.add(new F.Tuple<>(ANDROID_OS_VERSION, datapointTags.getAndroidOSVersion()));
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
        List<F.Tuple<String, String>> tags = new ArrayList<>();
        addReportLevelTags(tags, reportRequest);
        addDataPointLevelTags(tags, disk);

        dataPoints.add(new DataPoint(new Date(disk.getTimestamp()), measurements, tags));
    }
}