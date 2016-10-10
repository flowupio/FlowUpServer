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

        return insertDataPoints.execute(metrics).thenApply(response -> {
                    ReportResponse reportResponse = new ReportResponse();
                    reportResponse.message = "Metrics Inserted";
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

    List<DataPoint> mapNetwork(ReportRequest reportRequest) {
        List<DataPoint> dataPoints = new ArrayList<>();

        reportRequest.getNetwork().forEach((network) -> {
            List<F.Tuple<String, Value>> measurements = new ArrayList<>();
            measurements.add(new F.Tuple<>(BYTES_UPLOADED, Value.toBasicValue(network.getBytesUploaded())));
            measurements.add(new F.Tuple<>(BYTES_DOWNLOADED, Value.toBasicValue(network.getBytesDownloaded())));

            List<F.Tuple<String, String>> tags = new ArrayList<>();
            addReportLevelTags(tags, reportRequest);
            addDatapointLevelTags(tags, network);

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
            addDatapointLevelTags(tags, ui);
            tags.add(new F.Tuple<>(SCREEN_NAME, ui.getScreenName()));

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

    private void addDatapointLevelTags(List<F.Tuple<String, String>> tags, DatapointTags datapointTags) {
        tags.add(new F.Tuple<>(VERSION_NAME, datapointTags.getVersionName()));
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
        addDatapointLevelTags(tags, processingUnit);

        dataPoints.add(new DataPoint(new Date(processingUnit.getTimestamp()), measurements, tags));
    }
}