package usecases;

import com.feth.play.module.pa.providers.password.DefaultUsernamePasswordAuthUser;
import datasources.grafana.DashboardsClient;
import models.Application;
import models.User;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.inject.guice.GuiceApplicationBuilder;
import repositories.UserRepository;
import utils.WithFlowUpApplication;

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
public class ApplicationRepositoryTest extends WithFlowUpApplication {

    private static final String JOHN_GMAIL_COM = "john@gmail.com";
    private User user;
    @Mock
    private DashboardsClient dashboardsClient;

    @Override
    protected play.Application provideApplication() {
        when(dashboardsClient.createUser(any())).then(invocation -> {
            User user = invocation.getArgumentAt(0, User.class);
            user.setGrafanaUserId("2");
            user.setGrafanaPassword("GrafanaPassword");
            user.save();
            return CompletableFuture.completedFuture(user);
        });
        when(dashboardsClient.createDatasource(any())).then(invocation -> CompletableFuture.completedFuture(invocation.getArgumentAt(0, Application.class)));
        when(dashboardsClient.addUserToOrganisation(any(), any())).then(invocation -> CompletableFuture.completedFuture(invocation.getArgumentAt(1, Application.class)));
        when(dashboardsClient.deleteUserInDefaultOrganisation(any())).then(invocation -> CompletableFuture.completedFuture(invocation.getArgumentAt(0, User.class)));
        when(dashboardsClient.createOrg(any())).then(invocation -> {
            Application application = invocation.getArgumentAt(0, Application.class);
            application.setGrafanaOrgId("2");
            application.setOrganization(user.getOrganizations().get(0));
            application.save();
            return CompletableFuture.completedFuture(application);
        });

        return new GuiceApplicationBuilder()
                .overrides(bind(DashboardsClient.class).toInstance(dashboardsClient))
                .build();
    }


    @Override
    @Before
    public void startPlay() {
        super.startPlay();
        UserRepository userRepository = this.app.injector().instanceOf(UserRepository.class);
        DefaultUsernamePasswordAuthUser authUser = mock(DefaultUsernamePasswordAuthUser.class);
        when(authUser.getEmail()).thenReturn(JOHN_GMAIL_COM);
        try {
            this.user = userRepository.create(authUser).toCompletableFuture().get();
        } catch (InterruptedException | ExecutionException e) {
            this.user = null;
        }
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