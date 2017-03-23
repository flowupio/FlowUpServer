package installationscounter.usecase;

import installationscounter.domain.Installation;
import installationscounter.domain.InstallationsCounterRepository;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class IncrementInstallationsCounter {

    private final InstallationsCounterRepository repository;

    @Inject
    public IncrementInstallationsCounter(InstallationsCounterRepository repository) {
        this.repository = repository;
    }

    public CompletionStage<Installation> execute(Installation installation) {
        return null;
    }
}
