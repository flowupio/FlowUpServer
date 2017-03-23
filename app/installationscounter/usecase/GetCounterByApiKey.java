package installationscounter.usecase;

import installationscounter.domain.InstallationsCounterRepository;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class GetCounterByApiKey {

    private final InstallationsCounterRepository repository;

    @Inject
    public GetCounterByApiKey(InstallationsCounterRepository repository) {
        this.repository = repository;
    }

    public CompletionStage<Long> execute(String apiKeyValue) {
        return null;
    }
}
