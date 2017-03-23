package installationscounter.api;

import datasources.elasticsearch.ElasticsearchClient;
import installationscounter.domain.Installation;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class InstallationsCounterApiClient {

    private final ElasticsearchClient elasticClient;

    @Inject
    public InstallationsCounterApiClient(ElasticsearchClient elasticClient) {
        this.elasticClient = elasticClient;
    }

    public CompletionStage<Installation> incrementCounter(Installation installation) {
        return null;
    }

    public CompletionStage<Long> getInstallationCounter(String apiKey) {
        return null;
    }
}
