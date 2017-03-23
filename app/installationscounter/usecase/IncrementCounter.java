package installationscounter.usecase;

import installationscounter.domain.Installation;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class IncrementCounter {

    private final InstallationsCounter installationsCounter;

    @Inject
    public IncrementCounter(InstallationsCounter installationsCounter) {
        this.installationsCounter = installationsCounter;
    }

    public CompletionStage<Installation> execute(String apiKeyValue) {
        return null;
    }
}
