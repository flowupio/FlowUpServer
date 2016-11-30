package controllers.api;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.api.ApiKeySecured;
import controllers.api.HeaderParsers;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Security;
import sampling.SamplingGroup;
import usecases.models.ApiKeyConfig;
import usecases.GetApiKeyConfig;

import javax.inject.Inject;

import static play.mvc.Controller.request;
import static play.mvc.Http.Status.PRECONDITION_FAILED;
import static play.mvc.Results.ok;
import static play.mvc.Results.status;

@Security.Authenticated(ApiKeySecured.class)
public class ConfigController {

    private static final String X_API_KEY = "X-Api-Key";


    private final GetApiKeyConfig getConfig;

    @Inject
    public ConfigController(GetApiKeyConfig getConfig) {
        this.getConfig = getConfig;
    }

    public Result getConfig() {
        ApiKeyConfig apiKeyConfig = getApiKeyConfig();
        JsonNode content = Json.toJson(apiKeyConfig);
        return ok(content);
    }

    private ApiKeyConfig getApiKeyConfig() {
        String apiKeyValue = request().getHeader(X_API_KEY);
        String uuid = request().getHeader(HeaderParsers.X_UUID);
        return getConfig.execute(apiKeyValue, uuid);
    }

}
