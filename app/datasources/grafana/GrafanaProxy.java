package datasources.grafana;

import com.fasterxml.jackson.databind.node.ObjectNode;
import models.User;
import org.jetbrains.annotations.NotNull;
import play.Configuration;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSCookie;
import play.mvc.Call;
import play.mvc.Http;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class GrafanaProxy {
    public static final String LOGIN = "/login";
    private final WSClient ws;
    private final String baseUrl;

    @Inject
    public GrafanaProxy(WSClient ws, @Named("grafana") Configuration grafanaConf) {
        this.ws = ws;

        String scheme = grafanaConf.getString("scheme");
        String host = grafanaConf.getString("host");
        String port = grafanaConf.getString("port");
        this.baseUrl = scheme + "://" + host + ":" + port;
    }

    public CompletionStage<List<Http.Cookie>> retreiveSessionCookies(User user) {
        ObjectNode userRequest = Json.newObject()
                .put("user", user.getEmail())
                .put("email", "") // Empty by API definition of grafana
                .put("password", user.getGrafanaPassword());

        return ws.url(this.baseUrl + LOGIN).post(userRequest).thenApply(wsResponse ->
                wsResponse.getCookies().stream().map(this::toHttpCookie).collect(Collectors.toList())
        );
    }

    @NotNull
    private Http.Cookie toHttpCookie(WSCookie wsCookie) {
        return new Http.Cookie(wsCookie.getName(), wsCookie.getValue(), wsCookie.getMaxAge().intValue(), wsCookie.getPath(),
                wsCookie.getDomain(), wsCookie.isSecure(), true);
    }

    public String getHomeUrl() {
        return this.baseUrl;
    }
}
