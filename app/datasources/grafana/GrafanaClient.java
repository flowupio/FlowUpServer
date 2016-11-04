package datasources.grafana;

import com.fasterxml.jackson.databind.node.ObjectNode;
import datasources.elasticsearch.ElasticSearchDatasource;
import models.Application;
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

import javax.inject.Inject;
import javax.inject.Named;
import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public class GrafanaClient implements DashboardsClient {

    private static final String API_ORG = "/api/orgs";
    private static final String API_ORGS_ORG_ID_USERS = "/api/orgs/:orgId/users";
    private static final String API_ORGS_ORG_ID_USERS_USER_ID = "/api/orgs/:orgId/users/:userId";

    private static final String API_DATASOURCE = "/api/datasources";
    private static final String API_ADMIN_USERS = "/api/admin/users";
    private static final String API_USER_USING_ORGANISATION_ID = "/api/user/using/:organisationId";


    private final WSClient ws;
    private final String baseUrl;
    private final String apiKey;
    private final String adminUser;
    private final String adminPassword;
    private final String elasticsearchEndpoint;

    @Inject
    public GrafanaClient(WSClient ws, @Named("grafana") Configuration grafanaConf, @Named("elasticsearch") Configuration elasticsearchConf) {
        this.ws = ws;

        this.apiKey = grafanaConf.getString("api_key");
        this.adminUser = grafanaConf.getString("admin_user");
        this.adminPassword = grafanaConf.getString("admin_password");

        this.baseUrl = getGrafanaBaseUrl(grafanaConf);

        this.elasticsearchEndpoint = getElasticSearchEndpoint(elasticsearchConf);
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
    public CompletionStage<GrafanaResponse> createUser(final User user) {

        String grafanaPassword = PasswordGenerator.generatePassword();
        ObjectNode userRequest = Json.newObject()
                .put("name", user.getName())
                .put("email", user.getEmail())
                .put("password", grafanaPassword);

        UUID userId = user.getId();

        Logger.debug(userRequest.toString());

        return post(API_ADMIN_USERS, userRequest).thenApply(response -> {
            if (response.getStatus() == Http.Status.OK) {
                updateUserWithGrafanaInfo(grafanaPassword, userId, response);
            }
            return response;
        });
    }

    @Override
    public CompletionStage<GrafanaResponse> createOrg(Application application) {
        String orgName = application.getOrganization().getName() + " " + application.getAppPackage();
        ObjectNode request = Json.newObject()
                .put("name", orgName);

        Logger.debug(request.toString());

        return post(API_ORG, request).thenApply(response -> {
            if (response.getStatus() == Http.Status.OK) {
                updateApplicationWithGrafanaInfo(application.getId(), response);
            }
            return response;
        });
    }

    @Override
    public CompletionStage<GrafanaResponse> addUserToOrganisation(User user, Application application) {
        String adminUserEndpoint = API_ORGS_ORG_ID_USERS.replaceFirst(":orgId", application.getGrafanaOrgId());

        ObjectNode userRequest = Json.newObject()
                .put("loginOrEmail", user.getEmail())
                .put("role", "Viewer");

        Logger.debug(userRequest.toString());
        return post(adminUserEndpoint, userRequest);
    }

    @Override
    public CompletionStage<GrafanaResponse> deleteUserInDefaultOrganisation(User user) {
        String adminUserEndpoint = API_ORGS_ORG_ID_USERS_USER_ID.replaceFirst(":orgId", "1").replaceFirst(":userId", user.getGrafanaUserId());

        return delete(adminUserEndpoint);
    }

    @Override
    public CompletionStage<GrafanaResponse> createDatasource(Application application) {
        this.switchUserContext(application);
        ObjectNode request = Json.newObject()
                .put("name", "default")
                .put("type", "elasticsearch")
                .put("url", this.elasticsearchEndpoint + ElasticSearchDatasource.FLOWUP + application.getAppPackage())
                .put("access", "proxy")
                .put("is_default", true)
                .put("basicAuth", false);

        Logger.debug(request.toString());
        return post(API_DATASOURCE, request);
    }

    private void updateApplicationWithGrafanaInfo(UUID applicationId, GrafanaResponse response) {
        Application application = Application.find.byId(applicationId);
        String orgId = response.getAdditionalProperties().get("orgId").toString();
        if (application != null) {
            application.setGrafanaOrgId(orgId);
            application.save();
        }
    }

    private void updateUserWithGrafanaInfo(String grafanaPassword, UUID userId, GrafanaResponse response) {
        String id = response.getAdditionalProperties().get("id").toString();
        User user = User.find.byId(userId);
        if (user != null) {
            user.setGrafanaUserId(id);
            user.setGrafanaPassword(grafanaPassword);
            user.save();
        }
    }

    private CompletionStage<GrafanaResponse> switchUserContext(Application application) {
        String adminUserEndpoint = API_USER_USING_ORGANISATION_ID.replaceFirst(":orgId", application.getGrafanaOrgId());
        ObjectNode request = Json.newObject();

        return post(adminUserEndpoint, request);
    }

    private CompletionStage<GrafanaResponse> post(String adminUserEndpoint, ObjectNode request) {
        return getWsRequestForAdminUser(adminUserEndpoint).post(request).thenApply(this::parseWsResponse);
    }

    private CompletionStage<GrafanaResponse> delete(String adminUserEndpoint) {
        return getWsRequestForAdminUser(adminUserEndpoint).delete().thenApply(this::parseWsResponse);
    }

    private WSRequest getWsRequestForAdminUser(String adminUserEndpoint) {
        WSRequest wsRequest = ws.url(baseUrl + adminUserEndpoint).setHeader("Accept", "application/json").setContentType("application/json")
                .setAuth(this.adminUser, this.adminPassword);
        Logger.debug(wsRequest.getHeaders().toString());
        return wsRequest;
    }

    private WSRequest getWsRequest(String adminUserEndpoint) {
        WSRequest wsRequest = ws.url(baseUrl + adminUserEndpoint).setHeader("Accept", "application/json").setContentType("application/json")
                .setHeader("Authorization", "Bearer " + this.apiKey);
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