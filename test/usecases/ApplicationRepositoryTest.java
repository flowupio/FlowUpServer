package usecases;

import com.avaje.ebean.Model;
import com.feth.play.module.pa.providers.password.DefaultUsernamePasswordAuthUser;
import com.google.common.collect.ImmutableMap;
import datasources.grafana.DashboardsClient;
import datasources.grafana.GrafanaResponse;
import models.Application;
import models.Organization;
import models.User;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.Helpers;
import play.test.WithApplication;
import repositories.UserRepository;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static play.inject.Bindings.bind;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationRepositoryTest extends WithApplication {

    private static final String JOHN_GMAIL_COM = "john@gmail.com";
    private User user;
    @Mock
    private DashboardsClient dashboardsClient;


    @Override
    protected play.Application provideApplication() {
        CompletableFuture<GrafanaResponse> grafanaResponseCompletableFuture = CompletableFuture.completedFuture(mock(GrafanaResponse.class));
        when(dashboardsClient.createUser(any())).thenReturn(grafanaResponseCompletableFuture);
        when(dashboardsClient.createOrg(any())).then(new Answer<CompletableFuture<Application>>() {
            @Override
            public CompletableFuture<Application> answer(InvocationOnMock invocation) throws Throwable {
                Application application = invocation.getArgumentAt(0, Application.class);
                application.setGrafanaOrgId("1");
                application.setOrganization(mock(Organization.class));
                return CompletableFuture.completedFuture(mock(Application.class));
            }
        });

        return new GuiceApplicationBuilder()
                .overrides(bind(DashboardsClient.class).toInstance(dashboardsClient))
                .build();
    }


    @Before
    public void setupUserDatabase() {
        UserRepository userRepository = this.app.injector().instanceOf(UserRepository.class);
        DefaultUsernamePasswordAuthUser authUser = mock(DefaultUsernamePasswordAuthUser.class);
        when(authUser.getEmail()).thenReturn(JOHN_GMAIL_COM);

        this.user = userRepository.create(authUser);
    }

    @After
    public void cleanupDatabase() {
        User.findByEmail("john@gmail.com").delete();
    }

    @Test
    public void whenApplicationIsCreatedGrafanaClientIsCalled() throws ExecutionException, InterruptedException {
        ApplicationRepository applicationRepository = givenApplicationRepository();
        String apiKeyValue = user.getOrganizations().get(0).getApiKey().getValue();

        CompletionStage<Application> applicationCompletionStage = applicationRepository.create(apiKeyValue, "com.example.app");

        assertThat(applicationCompletionStage.toCompletableFuture().get(), not(nullValue()));
    }

    @NotNull
    private ApplicationRepository givenApplicationRepository() {
        return this.app.injector().instanceOf(ApplicationRepository.class);
    }
}