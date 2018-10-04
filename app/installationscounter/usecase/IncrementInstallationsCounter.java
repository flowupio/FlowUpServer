package installationscounter.usecase;

import installationscounter.domain.Installation;
import installationscounter.domain.InstallationsCounter;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class IncrementInstallationsCounter {

    private final InstallationsCounter installationsCounter;

    @Inject
    public IncrementInstallationsCounter(InstallationsCounter installationsCounter) {
        this.installationsCounter = installationsCounter;
    }

    public CompletionStage<Installation> execute(Installation installation) {
        return installationsCounter.increment(installation);
    }
}
