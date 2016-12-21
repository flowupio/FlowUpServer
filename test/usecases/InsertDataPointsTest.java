package usecases;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import models.Application;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import usecases.models.Report;
import usecases.repositories.ApplicationRepository;
import utils.mothers.WithReportFixtures;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static utils.mothers.WithReportFixtures.*;

@RunWith(DataProviderRunner.class)
public class InsertDataPointsTest implements WithReportFixtures {

    @Mock
    private MetricsDatasource metricsDatasourceMock;
    @Mock
    private ApplicationRepository applicationRepositoryMock;

    private InsertDataPoints useCase;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        useCase = new InsertDataPoints(metricsDatasourceMock, applicationRepositoryMock);
    }

    @DataProvider
    public static Object[][] invalidReportProvider() {
        return new Object[][]{
                {reportWithNoAppPackage()},
                {reportForBackgroundDebug()}
        };
    }

    @Test
    @UseDataProvider("invalidReportProvider")
    public void nonAllowedReportShouldNotBeStored(Report report) throws Exception {
        CompletionStage<InsertResult> futureResult = useCase.execute(report);
        InsertResult result = futureResult.toCompletableFuture().get();

        verify(metricsDatasourceMock, never()).writeDataPoints(any(Report.class), any(Application.class));
        assertEquals(expectedResult(), result);
    }

    @DataProvider
    public static Object[][] validReportProvider() {
        return new Object[][]{
                {reportForDebug()},
                {reportForBackground()}
        };
    }

    @Test
    @UseDataProvider("validReportProvider")
    public void allowedReportForFoundApplicationShouldBeStored(Report report) throws Exception {
        Application application = new Application();
        givenApplicationRepositoryToFindApplication(application);
        givenMetricsDataSourceToWriteDataPoints(report, application);

        CompletionStage<InsertResult> futureResult = useCase.execute(report);
        InsertResult result = futureResult.toCompletableFuture().get();
        assertEquals(expectedResult(), result);

        verify(metricsDatasourceMock, times(1)).writeDataPoints(report, application);
    }

    @Test
    @UseDataProvider("validReportProvider")
    public void allowedReportForNotFoundApplicationShouldBeStoredAfterCreateTheApplication(Report report) throws Exception {
        Application application = new Application();
        givenApplicationRepositoryToFindApplication(null);
        givenApplicationRepositoryToCreateAnAppliation(application);
        givenMetricsDataSourceToWriteDataPoints(report, application);

        CompletionStage<InsertResult> futureResult = useCase.execute(report);
        InsertResult result = futureResult.toCompletableFuture().get();
        assertEquals(expectedResult(), result);

        verify(applicationRepositoryMock, times(1)).create(report.getApiKey(), report.getAppPackage());
        verify(metricsDatasourceMock, times(1)).writeDataPoints(report, application);
    }

    private InsertResult expectedResult() {
        return new InsertResult(false, false, Collections.emptyList());
    }

    private void givenApplicationRepositoryToFindApplication(Application application) {
        when(applicationRepositoryMock.getApplicationByApiKeyValueAndAppPackage(anyString(), anyString()))
                .thenReturn(application);
    }

    private void givenApplicationRepositoryToCreateAnAppliation(Application application) {
        when(applicationRepositoryMock.create(anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(application));
    }

    private void givenMetricsDataSourceToWriteDataPoints(Report report, Application application) {
        when(metricsDatasourceMock.writeDataPoints(report, application))
                .thenReturn(CompletableFuture.completedFuture(expectedResult()));
    }

}