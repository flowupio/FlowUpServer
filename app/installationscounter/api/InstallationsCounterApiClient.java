package installationscounter.api;

import com.fasterxml.jackson.databind.JsonNode;
import datasources.elasticsearch.*;
import installationscounter.domain.Installation;
import play.libs.Json;
import utils.Time;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class InstallationsCounterApiClient {

    private static final String INSTALLATIONS_COUNTER_INDEX = "/installations/counter";
    private static final int INSTALLATIONS_COUNTER_TTL = 30;

    private final ElasticsearchClient elasticClient;
    private final Time time;

    @Inject
    public InstallationsCounterApiClient(ElasticsearchClient elasticClient, Time time) {
        this.elasticClient = elasticClient;
        this.time = time;
    }

    public CompletionStage<Installation> incrementCounter(Installation installation) {
        return elasticClient.post(INSTALLATIONS_COUNTER_INDEX, installation)
                .thenApply(result -> installation);
    }

    public CompletionStage<Long> getInstallationCounter(String apiKey) {
        SearchQuery query = getInstallationsQuery(apiKey);
        return elasticClient.performQuery(INSTALLATIONS_COUNTER_INDEX + "_search?",
                query.getSearchBody()).thenApply(result -> {
                    JsonNode buckets = result.getAggregations().get("group_by_state").get("buckets");
                    return buckets == null ? 0L : buckets.size();
                }
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
        JsonNode jsonFilter = Json.toJson("{" +
                "  \"filter\": {" +
                "  \"term\": {" +
                "  \"apiKey\": \"" + apiKey + "\"" +
                "  }" +
                "  }," +
                "  \"aggs\": {" +
                "    \"group_by_state\": {" +
                "      \"terms\": {" +
                "        \"field\": \"uuid\"" +
                "      }" +
                "    }" +
                "  }" +
                "}");
        filtered.setFilter(jsonFilter);
        bodyQuery.setFiltered(filtered);
        SearchRange range = new SearchRange();
        SearchTimestamp timestamp = new SearchTimestamp();
        long startDeletingDate = time.daysAgo(INSTALLATIONS_COUNTER_TTL).toDate().getTime();
        timestamp.setLte(startDeletingDate);
        range.setTimestamp(timestamp);
        bodyQuery.setRange(range);
        searchBody.setQuery(bodyQuery);
        return searchQuery;
    }
}
