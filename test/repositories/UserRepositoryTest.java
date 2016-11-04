package repositories;

import com.avaje.ebean.Model;
import com.feth.play.module.pa.providers.password.DefaultUsernamePasswordAuthUser;
import datasources.database.OrganizationDatasource;
import datasources.grafana.DashboardsClient;
import datasources.grafana.GrafanaClient;
import datasources.grafana.GrafanaResponse;
import models.User;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.WithApplication;

import java.util.concurrent.CompletableFuture;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static play.inject.Bindings.bind;

@RunWith(MockitoJUnitRunner.class)
public class UserRepositoryTest extends WithApplication {

    @Mock
    private DashboardsClient dashboardsClient;


    @Override
    protected Application provideApplication() {
        CompletableFuture<GrafanaResponse> grafanaResponseCompletableFuture = CompletableFuture.completedFuture(mock(GrafanaResponse.class));
        when(dashboardsClient.createUser(any())).thenReturn(grafanaResponseCompletableFuture);

        return new GuiceApplicationBuilder()
                .overrides(bind(DashboardsClient.class).toInstance(dashboardsClient))
                .build();
    }

    @Test
    public void whenUserWithGmailAccountIsCreatedNewOrgIsCreated() {
        UserRepository userRepository = givenUserRepository();
        DefaultUsernamePasswordAuthUser authUser = mock(DefaultUsernamePasswordAuthUser.class);
        when(authUser.getEmail()).thenReturn("john@gmail.com");

        User user = userRepository.create(authUser);

        assertThat(user.getOrganizations(), hasSize(1));
        user.getOrganizations().forEach(Model::delete);
        user.delete();
    }

    @Test
    public void whenUserWithGoogleAppsAccountIsCreatedNewOrgIsCreated() {
        UserRepository userRepository = givenUserRepository();
        DefaultUsernamePasswordAuthUser authUser = mock(DefaultUsernamePasswordAuthUser.class);
        when(authUser.getEmail()).thenReturn("john@example.com");

        User user = userRepository.create(authUser);

        assertThat(user.getOrganizations(), hasSize(1));
        user.getOrganizations().forEach(Model::delete);
        user.delete();
    }

    @Test
    public void whenUserWithGoogleAppsAccountIsCreatedWithAnAlreadyExistingOrg() {
        UserRepository userRepository = givenUserRepositoryWithOneOrganization("Example", "@example.com");
        DefaultUsernamePasswordAuthUser authUser = mock(DefaultUsernamePasswordAuthUser.class);
        when(authUser.getEmail()).thenReturn("john@example.com");

        User user = userRepository.create(authUser);

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
}