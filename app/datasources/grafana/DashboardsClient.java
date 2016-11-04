package datasources.grafana;

import models.Application;
import models.User;

import java.util.concurrent.CompletionStage;

public interface DashboardsClient {
    CompletionStage<GrafanaResponse> createUser(User user);

    CompletionStage<GrafanaResponse> createOrg(Application application);

    CompletionStage<GrafanaResponse> addUserToOrganisation(User user, Application application);

    CompletionStage<GrafanaResponse> deleteUserInDefaultOrganisation(User user);

    CompletionStage<GrafanaResponse> createDatasource(Application application);
}
