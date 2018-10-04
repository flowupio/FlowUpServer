package controllers.api;

import installationscounter.domain.Installation;
import installationscounter.usecase.IncrementInstallationsCounter;
import models.Version;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import security.ApiKeySecuredAction;
import usecases.GetApiKeyConfig;
import usecases.models.ApiKeyConfig;
import utils.Time;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.mvc.Controller.request;
import static play.mvc.Results.badRequest;
import static play.mvc.Results.ok;

@With(ApiKeySecuredAction.class)
public class ConfigController {

    private static final String X_API_KEY = "X-Api-Key";

    private final GetApiKeyConfig getConfig;
    private final IncrementInstallationsCounter incrementInstallationsCounter;
    private final Time time;

    @Inject
    public ConfigController(GetApiKeyConfig getConfig, IncrementInstallationsCounter incrementInstallationsCounter, Time time) {
        this.getConfig = getConfig;
        this.incrementInstallationsCounter = incrementInstallationsCounter;
        this.time = time;
    }

    public CompletionStage<Result> getConfig() {
        if (!isValidRequest(request())) {
            return completedFuture(badRequest());
        }
        return getApiKeyConfig().thenApply(apiKeyConfig -> ok(Json.toJson(apiKeyConfig)));
    }

    private boolean isValidRequest(Http.Request request) {
        return getApiKey(request) != null && getUUID(request) != null && !getVersion(request).equals(Version.UNKNOWN_VERSION);
    }

    private CompletionStage<ApiKeyConfig> getApiKeyConfig() {
        Http.Request request = request();
        String apiKeyValue = getApiKey(request);
        String uuid = getUUID(request);
        Version version = getVersion(request);
        incrementInstallationsCounter.execute(new Installation(apiKeyValue, uuid, version, time.now().getMillis()));
        return getConfig.execute(apiKeyValue, uuid, version);
    }

    private Version getVersion(Http.Request request) {
        return Version.fromString(request.getHeader(HeaderParsers.USER_AGENT));
    }

    private String getUUID(Http.Request request) {
        return request.getHeader(HeaderParsers.X_UUID);
    }

    private String getApiKey(Http.Request request) {
        return request.getHeader(X_API_KEY);
    }

}
