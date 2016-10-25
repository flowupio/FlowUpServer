package datasources.grafana;

import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Organization;
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

public class GrafanaClient {

    private static final String API_ORGS_ORG_ID_USERS = "/api/orgs/:orgId/users";
    private static final String API_ORGS_ORG_ID_USERS_USER_ID = "/api/orgs/:orgId/users/:userId";
    private final WSClient ws;
    private final String baseUrl;
    private final String apiKey;
    private final String adminUser;
    private final String adminPassword;

    @Inject
    public GrafanaClient(WSClient ws, @Named("grafana") Configuration grafanaConf) {
        this.ws = ws;

        this.apiKey = grafanaConf.getString("api_key");
        this.adminUser = grafanaConf.getString("admin_user");
        this.adminPassword = grafanaConf.getString("admin_password");

        String scheme = grafanaConf.getString("scheme");
        String host = grafanaConf.getString("host");
        String port = grafanaConf.getString("port");
        this.baseUrl = scheme + "://" + host + ":" + port;
    }

    public CompletionStage<GrafanaResponse> createUser(final User user) {
        String adminUserEndpoint = "/api/admin/users";

        String grafanaPassword = PasswordGenerator.generatePassword();
        ObjectNode userRequest = Json.newObject()
                .put("name", user.getName())
                .put("email", user.getEmail())
                .put("password", grafanaPassword);

        UUID userId = user.getId();

        Logger.debug(userRequest.toString());

        return getWsRequestForAdminUser(adminUserEndpoint).post(userRequest).thenApply(response -> {
            Logger.debug(response.getBody());
            if (response.getStatus() == Http.Status.OK) {
                updateUserWithGrafanaInfo(grafanaPassword, userId, response);
            }
            return Json.fromJson(response.asJson(), GrafanaResponse.class);
        });
    }

    private void updateUserWithGrafanaInfo(String grafanaPassword, UUID userId, WSResponse response) {
        String id = response.asJson().get("id").toString();
        User createdUser = User.find.byId(userId);
        createdUser.setGrafanaUserId(id);
        createdUser.setGrafanaPassword(grafanaPassword);
        createdUser.save();
    }

    public CompletionStage<GrafanaResponse> addUserToOrganisation(User user, Organization organization) {
        String adminUserEndpoint = API_ORGS_ORG_ID_USERS.replaceFirst(":orgId", organization.grafanaId);

        ObjectNode userRequest = Json.newObject()
                .put("loginOrEmail", user.getEmail())
                .put("role", "Viewer");

        UUID userId = user.getId();

        Logger.debug(userRequest.toString());

        return getWsRequestForAdminUser(adminUserEndpoint).post(userRequest).thenApply(response -> {
            Logger.debug(response.getBody());
            return Json.fromJson(response.asJson(), GrafanaResponse.class);
        });
    }

    public CompletionStage<GrafanaResponse> deleteUserInDefaultOrganisation(User user) {
        String adminUserEndpoint = API_ORGS_ORG_ID_USERS_USER_ID.replaceFirst(":orgId", "1").replaceFirst(":userId", user.getGrafanaUserId());

        return getWsRequestForAdminUser(adminUserEndpoint).delete().thenApply(response -> {
            Logger.debug(response.getBody());
            return Json.fromJson(response.asJson(), GrafanaResponse.class);
        });
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
}

class PasswordGenerator {
    @NotNull
    static String generatePassword() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789~`!@#$%^&*()-_=+[{]}\\|;:\'\",<.>/?";
        return RandomStringUtils.random(255, 0, 0, false, false, characters.toCharArray(), new SecureRandom());
    }
}