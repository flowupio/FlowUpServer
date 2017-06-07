package utils;

import usecases.DashboardsClient;
import models.Application;
import models.User;

import java.util.concurrent.CompletableFuture;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public interface WithDashboardsClient {
    default DashboardsClient getMockDashboardsClient() {
        DashboardsClient dashboardsClient = mock(DashboardsClient.class);
        when(dashboardsClient.createUser(any())).then(invocation -> {
            User user = invocation.getArgumentAt(0, User.class);
            user.setGrafanaUserId("2");
            user.setGrafanaPassword("GrafanaPassword");
            user.save();
            return CompletableFuture.completedFuture(user);
        });
        when(dashboardsClient.updateHomeDashboard(any(), any())).then(invocation -> CompletableFuture.completedFuture(null));
        when(dashboardsClient.createDatasource(any())).then(invocation -> CompletableFuture.completedFuture(invocation.getArgumentAt(0, Application.class)));
        when(dashboardsClient.createDashboards(any(), any())).then(invocation -> CompletableFuture.completedFuture(null));
        when(dashboardsClient.addUserToOrganisation(any(), any())).then(invocation -> CompletableFuture.completedFuture(invocation.getArgumentAt(1, Application.class)));
        when(dashboardsClient.deleteUserInDefaultOrganisation(any())).then(invocation -> CompletableFuture.completedFuture(invocation.getArgumentAt(0, User.class)));
        when(dashboardsClient.createOrg(any())).then(invocation -> {
            models.Application application = invocation.getArgumentAt(0, models.Application.class);
            application.setGrafanaOrgId("2");
            application.save();
            return CompletableFuture.completedFuture(application);
        });
        when(dashboardsClient.switchUserContext(any(), any())).then(invocation -> CompletableFuture.completedFuture(invocation.getArgumentAt(1, Application.class)));
        return dashboardsClient;
    }
}
