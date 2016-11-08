package controllers;

import models.ApiKey;
import play.mvc.Http;
import play.mvc.Security;
import repositories.ApiKeyRepository;
import scala.Option;

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
        Option<String> apiKeyHeaderValue = ctx._requestHeader().headers().get(X_API_KEY);
        if (apiKeyHeaderValue.isDefined()) {
            ApiKey apiKey = repository.getApiKey(apiKeyHeaderValue.get());
            return apiKey == null ? null : apiKey.getValue();
        } else {
            return null;
        }
    }

}
