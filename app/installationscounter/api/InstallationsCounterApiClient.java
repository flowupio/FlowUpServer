package installationscounter.api;

import datasources.elasticsearch.ElasticsearchClient;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class InstallationsCounterApiClient {

    private final ElasticsearchClient elasticClient;

    @Inject
    public InstallationsCounterApiClient(ElasticsearchClient elasticClient) {
        this.elasticClient = elasticClient;
    }

    public CompletionStage<Long> getInstallationCounter(String apiKey) {
        return null;
    }
}
