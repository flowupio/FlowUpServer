package usecases;

import com.feth.play.module.pa.providers.password.DefaultUsernamePasswordAuthUser;
import datasources.database.OrganizationDatasource;
import models.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import play.inject.Injector;
import play.inject.guice.GuiceApplicationBuilder;
import usecases.repositories.ApplicationRepository;
import usecases.repositories.UserRepository;
import utils.WithDashboardsClient;
import utils.WithFlowUpApplication;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static play.inject.Bindings.bind;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationRepositoryTest extends WithFlowUpApplication implements WithDashboardsClient {

    private static final String JOHN_GMAIL_COM = "john@gmail.com";
    private static final String ANY_APP_PACKAGE = "io.flowup.example";
    private static final String KARUMI_ORG = "Karumi";
    private static final String ANY_KARUMI_EMAIL = "davide@karumi.com";
    private static final String FLOW_UP_ORG = "FlowUp";
    private static final String ANY_FLOWUP_EMAIL = "sergio@flowup.com";

    private ApplicationRepository applicationRepository;
    private OrganizationDatasource organizationDatasource;

    @Override
    protected play.Application provideApplication() {
        return new GuiceApplicationBuilder()
                .overrides(bind(DashboardsClient.class).toInstance(getMockDashboardsClient()))
                .build();
    }

    @Override
    @Before
    public void startPlay() {
        super.startPlay();
        Injector injector = app.injector();
        applicationRepository = injector.instanceOf(ApplicationRepository.class);
        organizationDatasource = injector.instanceOf(OrganizationDatasource.class);
    }


    @Test
    public void whenApplicationIsCreatedGrafanaClientIsCalled() throws ExecutionException, InterruptedException {
        User user = givenAUserAlreadyCreated();
        String apiKeyValue = user.getOrganizations().get(0).getApiKey().getValue();

        CompletionStage<Application> applicationCompletionStage = applicationRepository.create(apiKeyValue, ANY_APP_PACKAGE, Platform.ANDROID);

        assertThat(applicationCompletionStage.toCompletableFuture().get(), not(nullValue()));
    }

    @Test
    public void anApplicationNotPreviouslyCreatedDoesNotExist() {
        Application application = applicationRepository.getApplicationByApiKeyValueAndAppPackage(KARUMI_ORG, ANY_APP_PACKAGE);

        assertNull(application);
    }

    @Test
    public void anApplicationPreviouslyCreatedExist() throws Exception {
        givenAUserAlreadyCreated(ANY_KARUMI_EMAIL);
        ApiKey apiKey = givenAnApplication(KARUMI_ORG, ANY_APP_PACKAGE);

        Application application = applicationRepository.getApplicationByApiKeyValueAndAppPackage(apiKey.getValue(), ANY_APP_PACKAGE);

        assertNotNull(application);
    }

    @Test
    public void supportsTwoApplicationsWithTheSamePackageNameButDifferentOrgs() throws Exception {
        givenAUserAlreadyCreated(ANY_KARUMI_EMAIL);
        ApiKey anyApiKey = givenAnApplication(KARUMI_ORG, ANY_APP_PACKAGE);
        givenAUserAlreadyCreated(ANY_FLOWUP_EMAIL);
        ApiKey anyOtherApiKey = givenAnApplication(FLOW_UP_ORG, ANY_APP_PACKAGE);

        Application existFirstOrg = applicationRepository.getApplicationByApiKeyValueAndAppPackage(anyApiKey.getValue(), ANY_APP_PACKAGE);
        Application existSecondOrg = applicationRepository.getApplicationByApiKeyValueAndAppPackage(anyOtherApiKey.getValue(), ANY_APP_PACKAGE);

        assertNotNull(existFirstOrg);
        assertNotNull(existSecondOrg);
    }

    private ApiKey givenAnApplication(String orgName, String packageName) throws InterruptedException, ExecutionException {
        Organization organization = organizationDatasource.create(orgName);
        ApiKey apiKey = organization.getApiKey();
        applicationRepository.create(apiKey.getValue(), packageName, Platform.ANDROID).toCompletableFuture().get();
        return apiKey;
    }

    private User givenAUserAlreadyCreated() {
        return givenAUserAlreadyCreated(JOHN_GMAIL_COM);
    }

    private User givenAUserAlreadyCreated(String email) {
        CreateUser createUser = app.injector().instanceOf(CreateUser.class);
        DefaultUsernamePasswordAuthUser authUser = mock(DefaultUsernamePasswordAuthUser.class);
        when(authUser.getId()).thenReturn(email);
        when(authUser.getEmail()).thenReturn(email);
        try {
            return createUser.execute(authUser).toCompletableFuture().get();
        } catch (InterruptedException | ExecutionException e) {
            return null;
        }
    }

}