package usecases;

import models.Application;
import models.Platform;
import models.User;
import usecases.models.Dashboard;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public interface DashboardsClient {
    CompletionStage<User> createUser(User user);

    CompletionStage<Application> createOrg(Application application);

    CompletionStage<Application> addUserToOrganisation(User user, Application application);

    CompletionStage<User> deleteUserInDefaultOrganisation(User user);

    CompletionStage<Void> updateHomeDashboard(User user, Application application);

    CompletionStage<Application> createDatasource(Application application);

    CompletableFuture<Void> createDashboards(Application application, Platform platform);

    CompletionStage<Application> switchUserContext(User user, Application application);
}
