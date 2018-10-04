package usecases;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import emailsender.EmailSender;
import models.Application;
import models.Organization;
import models.Platform;
import models.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import usecases.models.Report;
import usecases.repositories.ApplicationRepository;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static utils.fixtures.ReportFixtures.*;

@RunWith(DataProviderRunner.class)
public class InsertDataPointsTest {

    @Mock
    private MetricsDatasource metricsDatasource;
    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private EmailSender emailSender;

    private InsertDataPoints useCase;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        useCase = new InsertDataPoints(metricsDatasource, applicationRepository, emailSender);
    }

    @DataProvider
    public static Object[][] invalidReportProvider() {
        return new Object[][]{
                {reportWithNoAppPackage()},
                {reportWithEmptyAppPackage()},
                {reportForBackgroundDebug()}
        };
    }

    @Test
    @UseDataProvider("invalidReportProvider")
    public void nonAllowedReportShouldNotBeStored(Report report) throws Exception {
        InsertResult result = sendReport(report);

        verify(metricsDatasource, never()).writeDataPoints(any(Report.class), any(Application.class));
        assertEquals(InsertResult.successEmpty(), result);
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
        givenThereIsAnApplicationAlreadyCreated(application);
        givenDataPointsAreWrittenProperly(report, application);

        InsertResult result = sendReport(report);
        assertEquals(InsertResult.successEmpty(), result);

        verify(metricsDatasource, times(1)).writeDataPoints(report, application);
    }

    @Test
    @UseDataProvider("validReportProvider")
    public void allowedReportForNotFoundApplicationShouldBeStoredAfterCreateTheApplication(Report report) throws Exception {
        Application app = givenAnApplication();
        givenTheApplicationIsCreatedProperly(app);
        givenDataPointsAreWrittenProperly(report, app);

        InsertResult result = sendReport(report);
        assertEquals(InsertResult.successEmpty(), result);

        verify(applicationRepository, times(1)).create(report.getApiKey(), report.getAppPackage(), report.getPlatform());
        verify(metricsDatasource, times(1)).writeDataPoints(report, app);
    }

    @Test
    @UseDataProvider("validReportProvider")
    public void sendsAFirstReportPersistedEmailIfTheReportReceivedIsTheFirstOne(Report report) throws Exception {
        Application app = givenAnApplication();
        givenTheApplicationIsCreatedProperly(app);
        givenDataPointsAreWrittenProperly(report, app);

        sendReport(report);

        verify(emailSender).sendFirstReportReceived(app);
    }

    @Test
    @UseDataProvider("validReportProvider")
    public void doesNotSendsTheFirstReportPersistedEmailIfTheReportReceivedIsNotTheFirstOne(Report report) throws Exception {
        Application app = givenAnApplication();
        givenThereIsAnApplicationAlreadyCreated(app);
        givenDataPointsAreWrittenProperly(report, app);

        sendReport(report);

        verify(emailSender, never()).sendFirstReportReceived(app);
    }

    private InsertResult sendReport(Report report) throws InterruptedException, java.util.concurrent.ExecutionException {
        CompletionStage<InsertResult> futureResult = useCase.execute(report);
        return futureResult.toCompletableFuture().get();
    }

    private void givenThereIsAnApplicationAlreadyCreated(Application application) {
        when(applicationRepository.getApplicationByApiKeyValueAndAppPackage(anyString(), anyString(), isA(Platform.class)))
                .thenReturn(application);
    }

    private void givenTheApplicationIsCreatedProperly(Application application) {
        when(applicationRepository.create(anyString(), anyString(), isA(Platform.class)))
                .thenReturn(CompletableFuture.completedFuture(application));
    }

    private void givenDataPointsAreWrittenProperly(Report report, Application application) {
        when(metricsDatasource.writeDataPoints(report, application))
                .thenReturn(CompletableFuture.completedFuture(InsertResult.successEmpty()));
    }

    private Application givenAnApplication() {
        List<User> members = new LinkedList<>();
        User user = new User();
        user.setName("Paco");
        user.setEmail("paco@karumi.com");
        members.add(user);
        Organization organization = new Organization();
        organization.setId(UUID.randomUUID());
        organization.setMembers(members);
        Application app = new Application();
        app.setId(UUID.randomUUID());
        app.setAppPackage("io.flowup.example");
        app.setOrganization(organization);
        return app;
    }

}