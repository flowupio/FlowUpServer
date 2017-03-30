package installationscounter.api;

import com.fasterxml.jackson.databind.JsonNode;
import datasources.elasticsearch.*;
import installationscounter.domain.Installation;
import utils.Time;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class InstallationsCounterApiClient {

    private static final String INSTALLATIONS_COUNTER_INDEX = "/installations/counter";
    private static final int INSTALLATIONS_COUNTER_TTL = 30;
    private static final String ROOT_AGGREGATION_KEY = "fiter-by-api-key-and-group-by-uuid";
    private static final String GROUP_BY_UUID_AGGREGATION_KEY = "group_by_uuid";

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
        return elasticClient.performQuery(INSTALLATIONS_COUNTER_INDEX + "/_search",
                query.getSearchBody()).thenApply(result -> {
                    JsonNode aggregations = result.getAggregations();
                    if (aggregations == null) {
                        return 0L;
                    }
                    JsonNode buckets = aggregations.get(ROOT_AGGREGATION_KEY).get(GROUP_BY_UUID_AGGREGATION_KEY).get("buckets");
                    return buckets == null ? 0L : buckets.size();
                }
        );
    }

    private SearchQuery getInstallationsQuery(String apiKey) {
        SearchQuery searchQuery = new SearchQuery();
        SearchIndex searchIndex = new SearchIndex();
        searchIndex.setIndex(INSTALLATIONS_COUNTER_INDEX);
        searchQuery.setSearchIndex(searchIndex);
        SearchBody searchBody = new SearchBody();
        SearchBodyQuery bodyQuery = new SearchBodyQuery();
        SearchRange range = new SearchRange();
        SearchTimestamp timestamp = new SearchTimestamp();
        long startDeletingDate = time.daysAgo(INSTALLATIONS_COUNTER_TTL).toDate().getTime();
        timestamp.setGte(startDeletingDate);
        range.setTimestamp(timestamp);
        bodyQuery.setRange(range);
        searchBody.setQuery(bodyQuery);
        configureAggregation(searchBody, apiKey);
        searchQuery.setSearchBody(searchBody);
        return searchQuery;
    }

    private void configureAggregation(SearchBody searchBody, String apiKey) {
        AggregationMap aggs = new AggregationMap();
        Aggregation aggregation = new Aggregation();

        Aggregation groupByUUID = new Aggregation();
        TermsAggregation termsAggregation = new TermsAggregation();
        termsAggregation.setField("uuid");
        groupByUUID.setTerms(termsAggregation);
        aggregation.setAggs(AggregationMap.singleton(GROUP_BY_UUID_AGGREGATION_KEY, groupByUUID));

        MatchAggregation filter = new MatchAggregation();
        filter.put("apiKey", apiKey);
        aggregation.setFilter(filter);
        aggs.put(ROOT_AGGREGATION_KEY, aggregation);
        searchBody.setAggs(aggs);
    }
}
