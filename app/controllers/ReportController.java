package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import usecases.InsertDataPoints;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class ReportController extends Controller {
    @Inject
    InsertDataPoints insertDataPoints;

    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> index() {
        JsonNode json = request().body().asJson();
        ReportRequest reportRequest = Json.fromJson(json, ReportRequest.class);

        return insertDataPoints.execute().thenApply(response -> {
                    ReportResponse reportResponse = new ReportResponse();
                    reportResponse.message = "Metrics Inserted";
                    return ok(Json.toJson(reportResponse));
                }
        );
    }
}

class ReportRequest {
    public String appPackage;
    public String deviceModel;
    public String screenDensity;
    public String screenSize;
    public String installationUUID;
    public int numberOfCores;
    public List<Network> network = new ArrayList<Network>();
    public List<Ui> ui = new ArrayList<Ui>();
    public List<Cpu> cpu = new ArrayList<Cpu>();
    public List<Gpu> gpu = new ArrayList<Gpu>();

    static class Network {
        public int timestamp;
        public String versionName;
        public String androidOSVersion;
        public boolean baterySaverOn;
        public int bytesUploaded;
        public int bytesDownloaded;
    }

    static class Ui {
        public int timestamp;
        public String versionName;
        public String androidOSVersion;
        public boolean baterySaverOn;
        public String screenName;
        public int frameTime;
        public StatisticalValue fps;
    }

    static class Cpu {
        public int timestamp;
        public String versionName;
        public String androidOSVersion;
        public boolean baterySaverOn;
        public int consumption;
    }

    static class Gpu {
        public int timestamp;
        public String versionName;
        public String androidOSVersion;
        public boolean baterySaverOn;
        public int consumption;
    }

    static class StatisticalValue {
        public int avg;
        public int p90;
    }
}


class ReportResponse {
    public String message;
}
