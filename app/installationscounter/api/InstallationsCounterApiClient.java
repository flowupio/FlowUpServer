package installationscounter.api;

import com.fasterxml.jackson.databind.JsonNode;
import datasources.elasticsearch.*;
import installationscounter.domain.Installation;
import play.libs.Json;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class InstallationsCounterApiClient {

    private static final String INSTALLATIONS_COUNTER_INDEX = "installationsCounter";
    private final ElasticsearchClient elasticClient;

    @Inject
    public InstallationsCounterApiClient(ElasticsearchClient elasticClient) {
        this.elasticClient = elasticClient;
    }

    public CompletionStage<Installation> incrementCounter(Installation installation) {
        return elasticClient.post(INSTALLATIONS_COUNTER_INDEX, installation)
                .thenApply(result -> installation);
    }

    public CompletionStage<Long> getInstallationCounter(String apiKey) {
        SearchQuery query = getInstallationsQuery(apiKey);
        return elasticClient.performQuery(INSTALLATIONS_COUNTER_INDEX, query.getSearchBody()).thenApply(result ->
                result.getHits().getTotal()
        );
    }

    private SearchQuery getInstallationsQuery(String apiKey) {
        SearchQuery searchQuery = new SearchQuery();
        SearchIndex searchIndex = new SearchIndex();
        searchIndex.setIndex(INSTALLATIONS_COUNTER_INDEX);
        searchQuery.setSearchIndex(searchIndex);
        SearchBody searchBody = searchQuery.getSearchBody();
        SearchBodyQuery bodyQuery = new SearchBodyQuery();
        SearchBodyQueryFiltered filtered = new SearchBodyQueryFiltered();
        JsonNode jsonFilter = Json.toJson("{apiKey: '" + apiKey + "' }");
        filtered.setFilter(jsonFilter);
        bodyQuery.setFiltered(filtered);
        searchBody.setQuery(bodyQuery);
        return searchQuery;
    }
}
