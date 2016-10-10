package controllers;

import play.libs.F;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import usecases.DataPoint;
import usecases.InsertDataPoints;
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



        return insertDataPoints.execute(dataPointMapper.mapNetwork(reportRequest)).thenApply(response -> {
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
}