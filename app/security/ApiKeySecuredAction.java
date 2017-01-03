package security;

import models.ApiKey;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import usecases.repositories.ApiKeyRepository;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ApiKeySecuredAction extends Action.Simple {

    private static final String X_API_KEY = "X-Api-Key";

    private final ApiKeyRepository repository;

    @Inject
    public ApiKeySecuredAction(ApiKeyRepository repository) {
        this.repository = repository;
    }

    @Override
    public CompletionStage<Result> call(Http.Context ctx) {
        return getApiKey(ctx).thenCompose(apiKey -> {
            if (apiKey != null) {
                return delegate.call(ctx);
            } else {
                Result unauthorized = unauthorized(views.html.defaultpages.unauthorized.render());
                return CompletableFuture.completedFuture(unauthorized);
            }
        });
    }

    private CompletionStage<ApiKey> getApiKey(Http.Context ctx) {
        String apiKeyHeaderValue = ctx.request().getHeader(X_API_KEY);
        if (apiKeyHeaderValue != null) {
            return repository.getApiKeyAsync(apiKeyHeaderValue);
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }
}
