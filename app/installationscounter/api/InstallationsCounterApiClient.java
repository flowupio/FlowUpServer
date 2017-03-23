package installationscounter.api;

import datasources.elasticsearch.ElasticsearchClient;

import javax.inject.Inject;

public class InstallationsCounterApiClient {

    private final ElasticsearchClient elasticClient;

    @Inject
    public InstallationsCounterApiClient(ElasticsearchClient elasticClient) {
        this.elasticClient = elasticClient;
    }
}
