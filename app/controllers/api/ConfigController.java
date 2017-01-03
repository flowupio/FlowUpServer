package controllers.api;

import play.libs.Json;
import play.mvc.Result;
import play.mvc.With;
import security.ApiKeySecuredAction;
import usecases.GetApiKeyConfig;
import usecases.models.ApiKeyConfig;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

import static play.mvc.Controller.request;
import static play.mvc.Results.ok;

@With(ApiKeySecuredAction.class)
public class ConfigController {

    private static final String X_API_KEY = "X-Api-Key";

    private final GetApiKeyConfig getConfig;

    @Inject
    public ConfigController(GetApiKeyConfig getConfig) {
        this.getConfig = getConfig;
    }

    public CompletionStage<Result> getConfig() {
        return getApiKeyConfig().thenApply(apiKeyConfig -> ok(Json.toJson(apiKeyConfig)));
    }

    private CompletionStage<ApiKeyConfig> getApiKeyConfig() {
        String apiKeyValue = request().getHeader(X_API_KEY);
        String uuid = request().getHeader(HeaderParsers.X_UUID);
        return getConfig.execute(apiKeyValue, uuid);
    }

}
