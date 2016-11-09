package repositories;

import com.avaje.ebean.Model;
import com.feth.play.module.pa.providers.password.DefaultUsernamePasswordAuthUser;
import datasources.database.OrganizationDatasource;
import datasources.grafana.DashboardsClient;
import models.Organization;
import models.User;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import usecases.ApplicationRepository;
import utils.WithFlowUpApplication;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static play.inject.Bindings.bind;

@RunWith(MockitoJUnitRunner.class)
public class UserRepositoryTest extends WithFlowUpApplication {

    @Mock
    private DashboardsClient dashboardsClient;

    @Override
    protected Application provideApplication() {
        when(dashboardsClient.createUser(any())).then(invocation -> {
            User user = invocation.getArgumentAt(0, User.class);
            user.setGrafanaUserId("2");
            user.setGrafanaPassword("GrafanaPassword");
            user.save();
            return CompletableFuture.completedFuture(user);
        });
        when(dashboardsClient.createDatasource(any())).then(invocation -> CompletableFuture.completedFuture(invocation.getArgumentAt(0, models.Application.class)));
        when(dashboardsClient.addUserToOrganisation(any(), any())).then(invocation -> CompletableFuture.completedFuture(invocation.getArgumentAt(1, models.Application.class)));
        when(dashboardsClient.deleteUserInDefaultOrganisation(any())).then(invocation -> CompletableFuture.completedFuture(invocation.getArgumentAt(0, User.class)));
        when(dashboardsClient.createOrg(any())).then(invocation -> {
            models.Application application = invocation.getArgumentAt(0, models.Application.class);
            application.setGrafanaOrgId("2");
            application.save();
            return CompletableFuture.completedFuture(application);
        });

        return new GuiceApplicationBuilder()
                .overrides(bind(DashboardsClient.class).toInstance(dashboardsClient))
                .build();
    }

    @Test
    public void whenUserWithGmailAccountIsCreatedNewOrgIsCreated() throws ExecutionException, InterruptedException {
        UserRepository userRepository = givenUserRepository();
        DefaultUsernamePasswordAuthUser authUser = mock(DefaultUsernamePasswordAuthUser.class);
        when(authUser.getEmail()).thenReturn("john@gmail.com");

        User user = userRepository.create(authUser).toCompletableFuture().get();

        assertThat(user.getOrganizations(), hasSize(1));
        user.getOrganizations().forEach(Model::delete);
        user.delete();
    }

    @Test
    public void whenUserWithGoogleAppsAccountIsCreatedNewOrgIsCreated() throws ExecutionException, InterruptedException {
        UserRepository userRepository = givenUserRepository();
        DefaultUsernamePasswordAuthUser authUser = mock(DefaultUsernamePasswordAuthUser.class);
        when(authUser.getEmail()).thenReturn("john@example.com");

        User user = userRepository.create(authUser).toCompletableFuture().get();

        assertThat(user.getOrganizations(), hasSize(1));
        user.getOrganizations().forEach(Model::delete);
        user.delete();
    }

    @Test
    public void whenUserWithGoogleAppsAccountIsCreatedWithAnAlreadyExistingOrg() throws ExecutionException, InterruptedException {
        UserRepository userRepository = givenUserRepositoryWithOneOrganization("Example", "@example.com");
        DefaultUsernamePasswordAuthUser authUser = mock(DefaultUsernamePasswordAuthUser.class);
        when(authUser.getEmail()).thenReturn("john@example.com");

        User user = userRepository.create(authUser).toCompletableFuture().get();

        assertThat(user.getOrganizations(), hasSize(1));
        user.getOrganizations().forEach(Model::delete);
        user.delete();
    }

    @Test
    public void whenUserWithGoogleAppsAccountIsCreatedWithAnAlreadyExistingOrgWithApplications() throws ExecutionException, InterruptedException {
        UserRepository userRepository = givenUserRepositoryWithOneOrganizationAndMoreThanOneApp("Example", "@example.com");
        DefaultUsernamePasswordAuthUser authUser = mock(DefaultUsernamePasswordAuthUser.class);
        when(authUser.getEmail()).thenReturn("john@example.com");

        User user = userRepository.create(authUser).toCompletableFuture().get();

        assertThat(user.getOrganizations(), hasSize(1));
        user.getOrganizations().forEach(Model::delete);
        user.delete();
    }


    @NotNull
    private UserRepository givenUserRepository() {
        return this.app.injector().instanceOf(UserRepository.class);
    }

    @NotNull
    private UserRepository givenUserRepositoryWithOneOrganization(String name, String gooogleAccount) {
        OrganizationDatasource organizationDatasource = this.app.injector().instanceOf(OrganizationDatasource.class);
        organizationDatasource.create(name, gooogleAccount);
        return this.app.injector().instanceOf(UserRepository.class);
    }

    @NotNull
    private UserRepository givenUserRepositoryWithOneOrganizationAndMoreThanOneApp(String name, String gooogleAccount) {
        OrganizationDatasource organizationDatasource = this.app.injector().instanceOf(OrganizationDatasource.class);
        Organization organization = organizationDatasource.create(name, gooogleAccount);

        ApplicationRepository applicationRepository = this.app.injector().instanceOf(ApplicationRepository.class);
        applicationRepository.create(organization.getApiKey().getValue(), "com.example.app1");
        applicationRepository.create(organization.getApiKey().getValue(), "com.example.app2");

        return this.app.injector().instanceOf(UserRepository.class);
    }
}