package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Security;
import usecases.ApiKeyConfig;
import usecases.GetApiKeyConfig;

import javax.inject.Inject;

import static play.mvc.Controller.request;
import static play.mvc.Results.ok;

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
        return getConfig.execute(apiKeyValue);
    }

}
