package usecases;

import com.feth.play.module.pa.providers.password.DefaultUsernamePasswordAuthUser;
import datasources.database.OrganizationDatasource;
import datasources.grafana.DashboardsClient;
import models.ApiKey;
import models.Application;
import models.Organization;
import models.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.inject.Injector;
import play.inject.guice.GuiceApplicationBuilder;
import usecases.repositories.ApplicationRepository;
import usecases.repositories.UserRepository;
import utils.WithFlowUpApplication;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static play.inject.Bindings.bind;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationRepositoryTest extends WithFlowUpApplication {

    private static final String JOHN_GMAIL_COM = "john@gmail.com";
    private static final String ANY_APP_PACKAGE = "io.flowup.example";
    private static final String KARUMI_ORG = "Karumi";
    private static final String ANY_KARUMI_EMAIL = "davide@karumi.com";
    private static final String FLOW_UP_ORG = "FlowUp";
    private static final String ANY_FLOWUP_EMAIL = "sergio@flowup.com";

    @Mock
    private DashboardsClient dashboardsClient;
    private ApplicationRepository applicationRepository;
    private OrganizationDatasource organizationDatasource;

    @Override
    protected play.Application provideApplication() {
        return new GuiceApplicationBuilder()
                .overrides(bind(DashboardsClient.class).toInstance(dashboardsClient))
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

        CompletionStage<Application> applicationCompletionStage = applicationRepository.create(apiKeyValue, ANY_APP_PACKAGE);

        assertThat(applicationCompletionStage.toCompletableFuture().get(), not(nullValue()));
    }

    @Test
    public void anApplicationNotPreviouslyCreatedDoesNotExist() {
        boolean exist = applicationRepository.exist(KARUMI_ORG, ANY_APP_PACKAGE);

        assertFalse(exist);
    }

    @Test
    public void anApplicationPreviouslyCreatedExist() throws Exception {
        givenAUserAlreadyCreated(ANY_KARUMI_EMAIL);
        ApiKey apiKey = givenAnApplication(KARUMI_ORG, ANY_APP_PACKAGE);

        boolean exist = applicationRepository.exist(apiKey.getValue(), ANY_APP_PACKAGE);

        assertTrue(exist);
    }

    @Test
    public void supportsTwoApplicationsWithTheSamePackageNameButDifferentOrgs() throws Exception {
        givenAUserAlreadyCreated(ANY_KARUMI_EMAIL);
        ApiKey anyApiKey = givenAnApplication(KARUMI_ORG, ANY_APP_PACKAGE);
        givenAUserAlreadyCreated(ANY_FLOWUP_EMAIL);
        ApiKey anyOtherApiKey = givenAnApplication(FLOW_UP_ORG, ANY_APP_PACKAGE);

        boolean existFirstOrg = applicationRepository.exist(anyApiKey.getValue(), ANY_APP_PACKAGE);
        boolean existSecondOrg = applicationRepository.exist(anyOtherApiKey.getValue(), ANY_APP_PACKAGE);

        assertTrue(existFirstOrg);
        assertTrue(existSecondOrg);
    }

    private ApiKey givenAnApplication(String orgName, String packageName) throws InterruptedException, ExecutionException {
        Organization organization = organizationDatasource.create(orgName);
        ApiKey apiKey = organization.getApiKey();
        applicationRepository.create(apiKey.getValue(), packageName).toCompletableFuture().get();
        return apiKey;
    }

    private User givenAUserAlreadyCreated() {
        return givenAUserAlreadyCreated(JOHN_GMAIL_COM);
    }

    private User givenAUserAlreadyCreated(String email) {
        when(dashboardsClient.createUser(any())).then(invocation -> {
            User user = invocation.getArgumentAt(0, User.class);
            return CompletableFuture.completedFuture(user);
        });
        when(dashboardsClient.createDatasource(any())).then(invocation -> CompletableFuture.completedFuture(invocation.getArgumentAt(0, Application.class)));
        when(dashboardsClient.addUserToOrganisation(any(), any())).then(invocation -> CompletableFuture.completedFuture(invocation.getArgumentAt(1, Application.class)));
        when(dashboardsClient.deleteUserInDefaultOrganisation(any())).then(invocation -> CompletableFuture.completedFuture(invocation.getArgumentAt(0, User.class)));
        when(dashboardsClient.createOrg(any())).then(invocation -> {
            Application application = invocation.getArgumentAt(0, Application.class);
            return CompletableFuture.completedFuture(application);
        });
        UserRepository userRepository = app.injector().instanceOf(UserRepository.class);
        DefaultUsernamePasswordAuthUser authUser = mock(DefaultUsernamePasswordAuthUser.class);
        when(authUser.getId()).thenReturn(email);
        when(authUser.getEmail()).thenReturn(email);
        try {
            return userRepository.create(authUser).toCompletableFuture().get();
        } catch (InterruptedException | ExecutionException e) {
            return null;
        }
    }

}