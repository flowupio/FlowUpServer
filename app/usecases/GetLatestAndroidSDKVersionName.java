package usecases;

import usecases.repositories.AndroidSDKVersionNameRepository;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class GetLatestAndroidSDKVersionName {

    private final AndroidSDKVersionNameRepository repository;

    @Inject
    public GetLatestAndroidSDKVersionName(AndroidSDKVersionNameRepository repository) {
        this.repository = repository;
    }

    public CompletionStage<String> execute() {
        return repository.getLatestAndroidSDKVersionName();
    }
}
