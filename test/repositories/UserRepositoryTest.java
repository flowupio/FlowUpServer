package repositories;

import com.feth.play.module.pa.providers.password.DefaultUsernamePasswordAuthUser;
import com.google.common.collect.ImmutableMap;
import datasources.database.OrganizationDatasource;
import datasources.database.UserDatasource;
import datasources.grafana.GrafanaClient;
import datasources.grafana.GrafanaResponse;
import models.User;
import org.jetbrains.annotations.NotNull;
import org.junit.Ignore;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.Helpers;
import play.test.WithApplication;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserRepositoryTest extends WithApplication {

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder()
                .configure((Map) Helpers.inMemoryDatabase("default", ImmutableMap.of(
                        "MODE", "MYSQL"
                )))
                .build();
    }

    @Test
    public void whenUserWithGmailAccountIsCreatedNewOrgIsCreated() {
        UserRepository userRepository = givenUserRepository();
        DefaultUsernamePasswordAuthUser authUser = mock(DefaultUsernamePasswordAuthUser.class);
        when(authUser.getEmail()).thenReturn("john@gmail.com");

        User user = userRepository.create(authUser);

        assertThat(user.getOrganizations(), hasSize(1));
    }

    @Test
    public void whenUserWithGoogleAppsAccountIsCreatedNewOrgIsCreated() {
        UserRepository userRepository = givenUserRepository();
        DefaultUsernamePasswordAuthUser authUser = mock(DefaultUsernamePasswordAuthUser.class);
        when(authUser.getEmail()).thenReturn("john@example.com");

        User user = userRepository.create(authUser);

        assertThat(user.getOrganizations(), hasSize(1));
    }

    @Test
    @Ignore
    public void whenUserWithGoogleAppsAccountIsCreatedWithAnAlreadyExistingOrg() {
        UserRepository userRepository = givenUserRepositoryWithOneOrganization("Example", "@example.com");
        DefaultUsernamePasswordAuthUser authUser = mock(DefaultUsernamePasswordAuthUser.class);
        when(authUser.getEmail()).thenReturn("john@example.com");

        User user = userRepository.create(authUser);

        assertThat(user.getOrganizations(), hasSize(1));
    }

    @NotNull
    private UserRepository givenUserRepository() {
        UserDatasource userDatasource = provideApplication().injector().instanceOf(UserDatasource.class);
        OrganizationDatasource organizationDatasource = provideApplication().injector().instanceOf(OrganizationDatasource.class);
        GrafanaClient grafanaClient = mock(GrafanaClient.class);

        CompletableFuture<GrafanaResponse> grafanaResponseCompletableFuture = CompletableFuture.completedFuture(mock(GrafanaResponse.class));
        when(grafanaClient.createUser(any())).thenReturn(grafanaResponseCompletableFuture);
        return new UserRepository(userDatasource, organizationDatasource, grafanaClient);
    }

    @NotNull
    private UserRepository givenUserRepositoryWithOneOrganization(String name, String gooogleAccount) {
        UserDatasource userDatasource = provideApplication().injector().instanceOf(UserDatasource.class);
        OrganizationDatasource organizationDatasource = provideApplication().injector().instanceOf(OrganizationDatasource.class);
        organizationDatasource.create(name, gooogleAccount);
        GrafanaClient grafanaClient = mock(GrafanaClient.class);

        CompletableFuture<GrafanaResponse> grafanaResponseCompletableFuture = CompletableFuture.completedFuture(mock(GrafanaResponse.class));
        when(grafanaClient.createUser(any())).thenReturn(grafanaResponseCompletableFuture);
        return new UserRepository(userDatasource, organizationDatasource, grafanaClient);
    }
}