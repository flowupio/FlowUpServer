package datasources.elasticsearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.spotify.futures.CompletableFutures;
import controllers.api.DataPointMapper;
import controllers.api.ReportRequest;
import models.Application;
import models.Organization;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Json;
import play.test.WithApplication;
import usecases.InsertResult;
import usecases.models.DataPoint;
import usecases.models.Metric;
import usecases.models.Report;
import utils.WithResources;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static play.inject.Bindings.bind;

@RunWith(MockitoJUnitRunner.class)
public class BufferedElasticSearchDatasourceTest extends WithApplication implements WithResources {

    private static final String ANY_FIELD = "AnyField";
    @Mock
    private ElasticsearchClient elasticsearchClient;

    @Override
    protected play.Application provideApplication() {
        return new GuiceApplicationBuilder()
                .overrides(bind(ElasticsearchClient.class).toInstance(elasticsearchClient))
                .configure("elasticsearch.min_request_list_size", 6)
                .configure("elasticsearch.max_buffer_size", 6)
                .build();
    }

    @Test
    public void givenAUniqueReportRequestWhenWriteDataPointsICalledThenReportIsBufferedAndReturnEmptyList() throws ExecutionException, InterruptedException {
        Report report = givenAReportWithXMetrics(1);
        Application application = givenAnyApplication();
        setupElasticSearchClient();
        ElasticSearchDatasource elasticSearchDatasource = app.injector().instanceOf(ElasticSearchDatasource.class);

        CompletionStage<InsertResult> insertResultCompletionStage = elasticSearchDatasource.writeDataPoints(report, application);
        InsertResult insertResult = insertResultCompletionStage.toCompletableFuture().get();

        assertEquals(insertResult, new InsertResult(false, false, Collections.emptyList()));
    }

    @Test
    public void givenAReportRequestWithTwoRecollectWhenWriteDataPointsICalledThenReportIsWritten() throws ExecutionException, InterruptedException {
        Report report = givenAReportWithXMetrics(2);
        Application application = givenAnyApplication();
        setupElasticSearchClient();
        ElasticSearchDatasource elasticSearchDatasource = app.injector().instanceOf(ElasticSearchDatasource.class);

        CompletionStage<InsertResult> insertResultCompletionStage = elasticSearchDatasource.writeDataPoints(report, application);
        InsertResult insertResult = insertResultCompletionStage.toCompletableFuture().get();

        assertTrue(insertResult.getItems().size() > 0);
    }

    @Test
    public void givenTwoDebugReportRequestWhenWriteDataPointsICalledThenTheFirstOneIsBufferedAndTheSecondIsWritten() throws ExecutionException, InterruptedException {
        Report report = givenAReportWithXMetrics(1);
        Application application = givenAnyApplication();
        setupElasticSearchClient();
        ElasticSearchDatasource elasticSearchDatasource = app.injector().instanceOf(ElasticSearchDatasource.class);

        CompletionStage<InsertResult> insertResultCompletionStage1 = elasticSearchDatasource.writeDataPoints(report, application);
        CompletionStage<InsertResult> insertResultCompletionStage2 = elasticSearchDatasource.writeDataPoints(report, application);

        CompletableFuture<List<InsertResult>> listCompletableFuture = CompletableFutures.allAsList(asList(insertResultCompletionStage1, insertResultCompletionStage2));

        List<InsertResult> insertResults = listCompletableFuture.get();
        assertTrue(insertResults.get(0).getItems().size() == 0);
        assertTrue(insertResults.get(1).getItems().size() > 0);
    }

    private void setupElasticSearchClient() {
        ActionWriteResponse networkDataResponse = new IndexResponse("flowup-network_data", "counter", "AVe4CB89xL5tw_jvDTTd", 1, true);
        networkDataResponse.setShardInfo(new ActionWriteResponse.ShardInfo(2, 1));
        BulkItemResponse[] responses = {new BulkItemResponse(0, "index", networkDataResponse)};
        BulkResponse bulkResponse = new BulkResponse(responses, 67);

        when(elasticsearchClient.postBulk(anyListOf(IndexRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(bulkResponse));
    }

    private Application givenAnyApplication() {
        Application application = mock(Application.class);
        Organization organization = mock(Organization.class);
        when(application.getOrganization()).thenReturn(organization);
        when(organization.getId()).thenReturn(UUID.randomUUID());
        return application;
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
