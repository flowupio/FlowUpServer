package installationscounter.usecase;

import installationscounter.domain.InstallationsCounter;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class GetInstallationsCounterByApiKey {

    private final InstallationsCounter installationsCounter;

    @Inject
    public GetInstallationsCounterByApiKey(InstallationsCounter installationsCounter) {
        this.installationsCounter = installationsCounter;
    }


    public CompletionStage<Long> execute(String apiKeyValue) {
        return installationsCounter.getInstallationsCounter(apiKeyValue);
    }
}
