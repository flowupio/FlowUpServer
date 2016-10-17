package controllers;

import play.libs.F;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import usecases.DataPoint;
import usecases.InsertDataPoints;
import usecases.Metric;
import usecases.Value;

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

        List<Metric> metrics = new ArrayList<>();
        metrics.add(new Metric("network_data", dataPointMapper.mapNetwork(reportRequest)));
        metrics.add(new Metric("ui_data", dataPointMapper.mapUi(reportRequest)));
        metrics.add(new Metric("cpu_data", dataPointMapper.mapCpu(reportRequest)));
        metrics.add(new Metric("gpu_data", dataPointMapper.mapGpu(reportRequest)));
        metrics.add(new Metric("memory_data", dataPointMapper.mapMemory(reportRequest)));
        metrics.add(new Metric("disk_data", dataPointMapper.mapDisk(reportRequest)));
        return insertDataPoints.execute(metrics).thenApply(result -> {
                    ReportResponse reportResponse = new ReportResponse("Metrics Inserted", result);
                    return created(Json.toJson(reportResponse));
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