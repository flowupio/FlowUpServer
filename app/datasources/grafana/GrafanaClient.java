package datasources.grafana;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import datasources.elasticsearch.ElasticSearchDatasource;
import models.Application;
import models.Platform;
import models.User;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import play.Configuration;
import play.Logger;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import play.mvc.Http;
import usecases.DashboardsClient;
import usecases.mapper.DashboardMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class GrafanaClient implements DashboardsClient {

    private static final String API_ORG = "/api/orgs";
    private static final String API_ORGS_ORG_ID_USERS = "/api/orgs/:orgId/users";
    private static final String API_ORGS_ORG_ID_USERS_USER_ID = "/api/orgs/:orgId/users/:userId";

    private static final String API_DATASOURCE = "/api/datasources";
    private static final String API_ADMIN_USERS = "/api/admin/users";
    private static final String API_USER_USING_ORGANISATION_ID = "/api/user/using/:orgId";

    private static final String API_DASHBOARDS = "/api/dashboards/db";

    private final WSClient ws;
    private final String baseUrl;
    private final String apiKey;
    private final String adminUser;
    private final String adminPassword;
    private final String elasticsearchEndpoint;
    private final DashboardMapper dashboardMapper;
    private final DashboardsDataSource dashboardsDataSource;

    @Inject
    public GrafanaClient(WSClient ws, @Named("grafana") Configuration grafanaConf, @Named("elasticsearch") Configuration elasticsearchConf, DashboardMapper dashboardMapper, DashboardsDataSource dashboardsDataSource) {
        this.ws = ws;

        this.apiKey = grafanaConf.getString("api_key");
        this.adminUser = grafanaConf.getString("admin_user");
        this.adminPassword = grafanaConf.getString("admin_password");

        this.baseUrl = getGrafanaBaseUrl(grafanaConf);

        this.elasticsearchEndpoint = getElasticSearchEndpoint(elasticsearchConf);
        this.dashboardMapper = dashboardMapper;
        this.dashboardsDataSource = dashboardsDataSource;
    }

    private String getGrafanaBaseUrl(@Named("grafana") Configuration grafanaConf) {
        String scheme = grafanaConf.getString("scheme");
        String host = grafanaConf.getString("host");
        String port = grafanaConf.getString("port");
        return scheme + "://" + host + ":" + port;
    }

    @NotNull
    private String getElasticSearchEndpoint(Configuration elasticsearchConf) {
        String scheme = elasticsearchConf.getString("scheme");
        String host = elasticsearchConf.getString("host");
        String port = elasticsearchConf.getString("port");
        return scheme + "://" + host + ":" + port;
    }

    @Override
    public CompletionStage<User> createUser(User user) {

        String grafanaPassword = PasswordGenerator.generatePassword();
        ObjectNode userRequest = Json.newObject()
                .put("name", user.getName())
                .put("email", user.getEmail())
                .put("password", grafanaPassword);

        Logger.debug(userRequest.toString());

        return post(API_ADMIN_USERS, userRequest).thenApply(response -> {
            if (response.getStatus() == Http.Status.OK) {
                return updateUserWithGrafanaInfo(grafanaPassword, user, response);
            }
            return user;
        });
    }

    @Override
    public CompletionStage<Application> createOrg(Application application) {
        String orgName = application.getOrganization().getName() + " " + application.getAppPackage();
        ObjectNode request = Json.newObject()
                .put("name", orgName);

        Logger.debug(request.toString());

        return post(API_ORG, request).thenApply(response -> {
            if (response.getStatus() == Http.Status.OK) {
                return updateApplicationWithGrafanaInfo(application.getId(), response);
            }
            return application;
        });
    }

    @Override
    public CompletionStage<Application> addUserToOrganisation(User user, Application application) {
        String adminUserEndpoint = API_ORGS_ORG_ID_USERS.replaceFirst(":orgId", application.getGrafanaOrgId());

        ObjectNode userRequest = Json.newObject()
                .put("loginOrEmail", user.getEmail())
                .put("role", "Viewer");

        Logger.debug(userRequest.toString());
        return post(adminUserEndpoint, userRequest).thenApply(grafanaResponse -> application);
    }

    @Override
    public CompletionStage<User> deleteUserInDefaultOrganisation(User user) {
        String adminUserEndpoint = API_ORGS_ORG_ID_USERS_USER_ID.replaceFirst(":orgId", "1").replaceFirst(":userId", user.getGrafanaUserId());

        return delete(adminUserEndpoint).thenApply(grafanaResponse -> user);
    }

    @Override
    public CompletionStage<Application> createDatasource(Application application) {
        return this.switchUserContext(application).thenCompose(applicationSwitched -> {
            ObjectNode jsonNode = Json.newObject().put("timeField", "@timestamp").put("esVersion", 2);
            JsonNode request = Json.newObject()
                    .put("orgId", application.getGrafanaOrgId())
                    .put("name", "default")
                    .put("type", "elasticsearch")
                    .put("url", this.elasticsearchEndpoint)
                    .put("database", ElasticSearchDatasource.indexName(application.getAppPackage(), application.getOrganization().getId().toString()))
                    .put("access", "proxy")
                    .put("isDefault", true)
                    .put("basicAuth", false)
                    .set("jsonData", jsonNode);

            Logger.debug(request.toString());
            return post(API_DATASOURCE, request).thenApply(grafanaResponse -> applicationSwitched);
        });
    }

    private Application updateApplicationWithGrafanaInfo(UUID applicationId, GrafanaResponse response) {
        Application application = Application.find.byId(applicationId);
        String orgId = response.getAdditionalProperties().get("orgId").toString();
        if (application != null) {
            application.setGrafanaOrgId(orgId);
            application.save();
        }
        return application;
    }

    private User updateUserWithGrafanaInfo(String grafanaPassword, User user, GrafanaResponse response) {
        String id = response.getAdditionalProperties().get("id").toString();
        user.setGrafanaUserId(id);
        user.setGrafanaPassword(grafanaPassword);
        user.save();
        return user;
    }

    public CompletionStage<Application> switchUserContext(User user, Application application) {
        String adminUserEndpoint = API_USER_USING_ORGANISATION_ID.replaceFirst(":orgId", application.getGrafanaOrgId());
        ObjectNode request = Json.newObject();

        return getWsRequestForUser(user.getEmail(), user.getGrafanaPassword(), adminUserEndpoint).post(request).thenApply(this::parseWsResponse).thenApply(grafanaResponse -> application);
    }

    @Override
    public CompletableFuture<Void> createDashboards(Platform platform) {
        List<CompletableFuture> requests = dashboardsDataSource.getDashboards(platform).stream()
                .map(dashboardMapper::map)
                .map(request -> post(API_DASHBOARDS, request).toCompletableFuture())
                .collect(Collectors.toList());
        return CompletableFuture.allOf(requests.toArray(new CompletableFuture[0]));
    }

    private CompletionStage<Application> switchUserContext(Application application) {
        String adminUserEndpoint = API_USER_USING_ORGANISATION_ID.replaceFirst(":orgId", application.getGrafanaOrgId());
        ObjectNode request = Json.newObject();

        return post(adminUserEndpoint, request).thenApply(grafanaResponse -> application);
    }

    private CompletionStage<GrafanaResponse> post(String adminUserEndpoint, JsonNode request) {
        return getWsRequestForAdminUser(adminUserEndpoint).post(request).thenApply(this::parseWsResponse);
    }

    private CompletionStage<GrafanaResponse> delete(String adminUserEndpoint) {
        return getWsRequestForAdminUser(adminUserEndpoint).delete().thenApply(this::parseWsResponse);
    }

    private WSRequest getWsRequestForAdminUser(String adminUserEndpoint) {
        return getWsRequestForUser(this.adminUser, this.adminPassword, adminUserEndpoint);
    }

    private WSRequest getWsRequestForUser(String username, String password, String adminUserEndpoint) {
        WSRequest wsRequest = ws.url(baseUrl + adminUserEndpoint).setHeader("Accept", "application/json").setContentType("application/json")
                .setAuth(username, password);
        Logger.debug(wsRequest.getHeaders().toString());
        return wsRequest;
    }

    @NotNull
    private GrafanaResponse parseWsResponse(WSResponse wsResponse) {
        Logger.debug(wsResponse.getAllHeaders().toString());
        Logger.debug(wsResponse.getBody());
        GrafanaResponse grafanaResponse = Json.fromJson(wsResponse.asJson(), GrafanaResponse.class);
        grafanaResponse.setStatus(wsResponse.getStatus());
        return grafanaResponse;
    }
}

class PasswordGenerator {
    @NotNull
    static String generatePassword() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789~`!@#$%^&*()-_=+[{]}\\|;:\'\",<.>/?";
        return RandomStringUtils.random(255, 0, 0, false, false, characters.toCharArray(), new SecureRandom());
    }
}