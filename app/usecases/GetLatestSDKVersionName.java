package usecases;

import models.Platform;
import usecases.repositories.SDKVersionNameRepository;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class GetLatestSDKVersionName {

    private final SDKVersionNameRepository repository;

    @Inject
    public GetLatestSDKVersionName(SDKVersionNameRepository repository) {
        this.repository = repository;
    }

    public CompletionStage<String> execute(Platform platform) {
        return repository.getLatestSDKVersionName(platform);
    }
}
