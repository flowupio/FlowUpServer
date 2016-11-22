package controllers.api;

import models.ApiKey;
import play.mvc.Http;
import play.mvc.Security;
import usecases.repositories.ApiKeyRepository;

import javax.inject.Inject;

class ApiKeySecured extends Security.Authenticator {

    private static final String X_API_KEY = "X-Api-Key";

    private final ApiKeyRepository repository;

    @Inject
    public ApiKeySecured(ApiKeyRepository repository) {
        this.repository = repository;
    }

    @Override
    public String getUsername(Http.Context ctx) {
        String apiKeyHeaderValue = ctx.request().getHeader(X_API_KEY);
        if (apiKeyHeaderValue != null) {
            ApiKey apiKey = repository.getApiKey(apiKeyHeaderValue);
            return apiKey == null ? null : apiKey.getValue();
        } else {
            return null;
        }
    }

}
