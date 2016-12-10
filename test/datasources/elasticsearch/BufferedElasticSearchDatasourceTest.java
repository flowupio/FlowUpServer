package datasources.elasticsearch;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.api.DataPointMapper;
import controllers.api.ReportRequest;
import models.Application;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.libs.Json;
import usecases.InsertResult;
import usecases.models.DataPoint;
import usecases.models.Metric;
import usecases.models.Report;
import utils.WithFlowUpApplication;
import utils.WithResources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static play.inject.Bindings.bind;


@RunWith(MockitoJUnitRunner.class)
public class BufferedElasticSearchDatasourceTest extends WithFlowUpApplication implements WithResources {

    private static final String EXPECTED_MESSAGE_BODY = "[{\"action\":{\"index\":{\"_index\":\"flowup-9cdc0b15-bdb0-4209-a3d2-3bc7012d9793-io.flowup.app\",\"_type\":\"network_data\"}},\"source\":{\"@timestamp\":123456789,\"BytesUploaded\":1024.0,\"BytesDownloaded\":2048.0,\"AppPackage\":\"io.flowup.example\",\"DeviceModel\":\"Nexus 5X\",\"ScreenDensity\":\"xxhdpi\",\"ScreenSize\":\"800X600\",\"InstallationUUID\":\"123456789\",\"NumberOfCores\":\"4\",\"VersionName\":\"1.0.0\",\"AndroidOSVersion\":\"API24\",\"BatterySaverOn\":\"true\"}},{\"action\":{\"index\":{\"_index\":\"flowup-9cdc0b15-bdb0-4209-a3d2-3bc7012d9793-io.flowup.app\",\"_type\":\"ui_data\"}},\"source\":{\"@timestamp\":123456789,\"FrameTime\":{\"mean\":60.0,\"p10\":60.0,\"p90\":60.0},\"FramesPerSecond\":{\"mean\":1.6666666666666666E7,\"p10\":1.6666666666666666E7,\"p90\":1.6666666666666666E7},\"OnActivityCreatedTime\":{\"mean\":60.0,\"p10\":60.0,\"p90\":60.0},\"OnActivityStartedTime\":{\"mean\":60.0,\"p10\":60.0,\"p90\":60.0},\"OnActivityResumedTime\":{\"mean\":60.0,\"p10\":60.0,\"p90\":60.0},\"ActivityTime\":{\"mean\":60.0,\"p10\":60.0,\"p90\":60.0},\"OnActivityPausedTime\":{\"mean\":60.0,\"p10\":60.0,\"p90\":60.0},\"OnActivityStoppedTime\":{\"mean\":60.0,\"p10\":60.0,\"p90\":60.0},\"OnActivityDestroyedTime\":{\"mean\":60.0,\"p10\":60.0,\"p90\":60.0},\"AppPackage\":\"io.flowup.example\",\"DeviceModel\":\"Nexus 5X\",\"ScreenDensity\":\"xxhdpi\",\"ScreenSize\":\"800X600\",\"InstallationUUID\":\"123456789\",\"NumberOfCores\":\"4\",\"VersionName\":\"1.0.0\",\"AndroidOSVersion\":\"API24\",\"BatterySaverOn\":\"true\",\"ScreenName\":\"MainActivity\"}},{\"action\":{\"index\":{\"_index\":\"flowup-9cdc0b15-bdb0-4209-a3d2-3bc7012d9793-io.flowup.app\",\"_type\":\"cpu_data\"}},\"source\":{\"@timestamp\":123456789,\"Consumption\":10.0,\"AppPackage\":\"io.flowup.example\",\"DeviceModel\":\"Nexus 5X\",\"ScreenDensity\":\"xxhdpi\",\"ScreenSize\":\"800X600\",\"InstallationUUID\":\"123456789\",\"NumberOfCores\":\"4\",\"VersionName\":\"1.0.0\",\"AndroidOSVersion\":\"API24\",\"BatterySaverOn\":\"true\"}},{\"action\":{\"index\":{\"_index\":\"flowup-9cdc0b15-bdb0-4209-a3d2-3bc7012d9793-io.flowup.app\",\"_type\":\"memory_data\"}},\"source\":{\"@timestamp\":123456789,\"Consumption\":3.0,\"BytesAllocated\":1024.0,\"AppPackage\":\"io.flowup.example\",\"DeviceModel\":\"Nexus 5X\",\"ScreenDensity\":\"xxhdpi\",\"ScreenSize\":\"800X600\",\"InstallationUUID\":\"123456789\",\"NumberOfCores\":\"4\",\"VersionName\":\"1.0.0\",\"AndroidOSVersion\":\"API24\",\"BatterySaverOn\":\"true\"}},{\"action\":{\"index\":{\"_index\":\"flowup-9cdc0b15-bdb0-4209-a3d2-3bc7012d9793-io.flowup.app\",\"_type\":\"disk_data\"}},\"source\":{\"@timestamp\":123456789,\"InternalStorageWrittenBytes\":2048.0,\"SharedPreferencesWrittenBytes\":1024.0,\"AppPackage\":\"io.flowup.example\",\"DeviceModel\":\"Nexus 5X\",\"ScreenDensity\":\"xxhdpi\",\"ScreenSize\":\"800X600\",\"InstallationUUID\":\"123456789\",\"NumberOfCores\":\"4\",\"VersionName\":\"1.0.0\",\"AndroidOSVersion\":\"API24\",\"BatterySaverOn\":\"true\"}}]";
    @Mock
    private AmazonSQS amazonSQS;

    @Captor
    private ArgumentCaptor<SendMessageRequest> sendMessageRequestArgumentCaptor;


    @Override
    protected play.Application provideApplication() {
        return getGuiceApplicationBuilder()
                .overrides(bind(AmazonSQS.class).toInstance(amazonSQS))
                .configure("elasticsearch.min_request_list_size", 6)
                .configure("elasticsearch.queue_small_request_enabled", true)
                .configure("elasticsearch.dry_run_queue_enabled", false)
                .build();
    }

    @Test
    public void givenAUniqueReportRequestWhenWriteDataPointsICalledThenReportIsBufferedAndReturnEmptyList() throws ExecutionException, InterruptedException {
        Report report = givenAReportWithXMetrics(1);
        Application application = givenAnyApplicationWithOrganizationId(UUID.fromString("9cdc0b15-bdb0-4209-a3d2-3bc7012d9793"));
        setupSuccessfulElasticsearchClient();
        ElasticSearchDatasource elasticSearchDatasource = app.injector().instanceOf(ElasticSearchDatasource.class);

        CompletionStage<InsertResult> insertResultCompletionStage = elasticSearchDatasource.writeDataPoints(report, application);
        InsertResult insertResult = insertResultCompletionStage.toCompletableFuture().get();

        verify(amazonSQS).sendMessage(sendMessageRequestArgumentCaptor.capture());
        assertEquals(EXPECTED_MESSAGE_BODY, sendMessageRequestArgumentCaptor.getValue().getMessageBody());
        assertEquals(insertResult, new InsertResult(false, false, Collections.emptyList()));
    }

    @Test
    public void givenAReportRequestWithTwoRecollectWhenWriteDataPointsICalledThenReportIsWritten() throws ExecutionException, InterruptedException {
        Report report = givenAReportWithXMetrics(2);
        Application application = givenAnyApplication();
        setupSuccessfulElasticsearchClient();
        ElasticSearchDatasource elasticSearchDatasource = app.injector().instanceOf(ElasticSearchDatasource.class);

        CompletionStage<InsertResult> insertResultCompletionStage = elasticSearchDatasource.writeDataPoints(report, application);
        InsertResult insertResult = insertResultCompletionStage.toCompletableFuture().get();

        assertTrue(insertResult.getItems().size() > 0);
    }

    @NotNull
    private Report givenAReportWithXMetrics(int nbMetrics) {
        String organizationIdentifier = "3e02e6b9-3a33-4113-ae78-7d37f11ca3bf";


        JsonNode jsonNode = Json.parse(getFile("androidsdk/simpleReportRequestBody.json"));
        ReportRequest reportRequest = Json.fromJson(jsonNode, ReportRequest.class);

        DataPointMapper dataPointMapper = new DataPointMapper();

        List<Metric> metrics = new ArrayList<>();
        metrics.add(new Metric("network_data", getAccumulatedDataPoints(nbMetrics, value -> dataPointMapper.mapNetwork(reportRequest))));
        metrics.add(new Metric("ui_data", getAccumulatedDataPoints(nbMetrics, value -> dataPointMapper.mapUi(reportRequest))));
        metrics.add(new Metric("cpu_data", getAccumulatedDataPoints(nbMetrics, value -> dataPointMapper.mapCpu(reportRequest))));
        metrics.add(new Metric("memory_data", getAccumulatedDataPoints(nbMetrics, value -> dataPointMapper.mapMemory(reportRequest))));
        metrics.add(new Metric("disk_data", getAccumulatedDataPoints(nbMetrics, value -> dataPointMapper.mapDisk(reportRequest))));
        return new Report(organizationIdentifier, "io.flowup.app", metrics);
    }

    private List<DataPoint> getAccumulatedDataPoints(int nbMetrics, IntFunction<List<DataPoint>> mapper) {
        return IntStream.range(0, nbMetrics)
                .mapToObj(mapper)
                .reduce(new ArrayList<>(), (dataPoints, dataPoints2) -> {
                    dataPoints.addAll(dataPoints2);
                    return dataPoints;
                });
    }
}
