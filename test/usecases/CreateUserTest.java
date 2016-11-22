package usecases;

import com.avaje.ebean.Model;
import com.feth.play.module.pa.providers.password.DefaultUsernamePasswordAuthUser;
import datasources.database.OrganizationDatasource;
import models.Organization;
import models.User;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import usecases.repositories.ApplicationRepository;
import usecases.repositories.UserRepository;
import utils.WithDashboardsClient;
import utils.WithFlowUpApplication;

import java.util.concurrent.ExecutionException;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static play.inject.Bindings.bind;

@RunWith(MockitoJUnitRunner.class)
public class CreateUserTest extends WithFlowUpApplication implements WithDashboardsClient {

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder()
                .overrides(bind(DashboardsClient.class).toInstance(getMockDashboardsClient()))
                .build();
    }

    @Test
    public void whenUserWithGmailAccountIsCreatedNewOrgIsCreated() throws ExecutionException, InterruptedException {
        CreateUser createUser = givenCreateUser();
        DefaultUsernamePasswordAuthUser authUser = mock(DefaultUsernamePasswordAuthUser.class);
        when(authUser.getEmail()).thenReturn("john@gmail.com");

        User user = createUser.execute(authUser).toCompletableFuture().get();

        assertThat(user.getOrganizations(), hasSize(1));
        user.getOrganizations().forEach(Model::delete);
        user.delete();
    }

    @Test
    public void whenUserWithGoogleAppsAccountIsCreatedNewOrgIsCreated() throws ExecutionException, InterruptedException {
        CreateUser createUser = givenCreateUser();
        DefaultUsernamePasswordAuthUser authUser = mock(DefaultUsernamePasswordAuthUser.class);
        when(authUser.getEmail()).thenReturn("john@example.com");

        User user = createUser.execute(authUser).toCompletableFuture().get();

        assertThat(user.getOrganizations(), hasSize(1));
        user.getOrganizations().forEach(Model::delete);
        user.delete();
    }

    @Test
    public void whenUserWithGoogleAppsAccountIsCreatedWithAnAlreadyExistingOrg() throws ExecutionException, InterruptedException {
        CreateUser createUser = givenCreateUserWithOneOrganization("Example", "@example.com");
        DefaultUsernamePasswordAuthUser authUser = mock(DefaultUsernamePasswordAuthUser.class);
        when(authUser.getEmail()).thenReturn("john@example.com");

        User user = createUser.execute(authUser).toCompletableFuture().get();

        assertThat(user.getOrganizations(), hasSize(1));
        user.getOrganizations().forEach(Model::delete);
        user.delete();
    }

    @Test
    public void whenUserWithGoogleAppsAccountIsCreatedWithAnAlreadyExistingOrgWithApplications() throws ExecutionException, InterruptedException {
        CreateUser createUser = givenCreateUserWithOneOrganizationAndMoreThanOneApp("Example", "@example.com");
        DefaultUsernamePasswordAuthUser authUser = mock(DefaultUsernamePasswordAuthUser.class);
        when(authUser.getEmail()).thenReturn("john@example.com");

        User user = createUser.execute(authUser).toCompletableFuture().get();

        assertThat(user.getOrganizations(), hasSize(1));
        user.getOrganizations().forEach(Model::delete);
        user.delete();
    }


    @NotNull
    private CreateUser givenCreateUser() {
        return this.app.injector().instanceOf(CreateUser.class);
    }

    @NotNull
    private CreateUser givenCreateUserWithOneOrganization(String name, String gooogleAccount) {
        OrganizationDatasource organizationDatasource = this.app.injector().instanceOf(OrganizationDatasource.class);
        organizationDatasource.create(name, gooogleAccount);
        return this.app.injector().instanceOf(CreateUser.class);
    }

    @NotNull
    private CreateUser givenCreateUserWithOneOrganizationAndMoreThanOneApp(String name, String gooogleAccount) {
        OrganizationDatasource organizationDatasource = this.app.injector().instanceOf(OrganizationDatasource.class);
        Organization organization = organizationDatasource.create(name, gooogleAccount);

        ApplicationRepository applicationRepository = this.app.injector().instanceOf(ApplicationRepository.class);
        applicationRepository.create(organization.getApiKey().getValue(), "com.example.app1");
        applicationRepository.create(organization.getApiKey().getValue(), "com.example.app2");

        return this.app.injector().instanceOf(CreateUser.class);
    }
}