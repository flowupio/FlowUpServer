package datasources.elasticsearch;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import play.Configuration;
import play.Logger;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import utils.Time;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;


public class ElasticsearchClient {

    private static final String BULK_ENDPOINT = "/_bulk";
    private static final String MSEARCH_ENDPOINT = "/_msearch";
    private static final String SEARCH_ENDPOINT = "/_search";

    private final WSClient ws;
    private final String baseUrl;

    @Inject
    public ElasticsearchClient(WSClient ws, @Named("elasticsearch") Configuration elasticsearchConf) {
        this.ws = ws;
        String scheme = elasticsearchConf.getString("scheme");
        String host = elasticsearchConf.getString("host");
        String port = elasticsearchConf.getString("port");
        this.baseUrl = scheme + "://" + host + ":" + port;
    }

    public CompletionStage<Void> deleteOldDataPoints() {
        CompletionStage<SearchResponse> response = getOldDataPoints();
        return response.thenCompose(new Function<SearchResponse, CompletionStage<Void>>() {
            @Override
            public CompletionStage<Void> apply(SearchResponse searchResponse) {
                List<DeleteRequest> indexesToDelete = mapToDeleteRequests(searchResponse.getHits());
                return deleteBulk(indexesToDelete).thenApply(wsResponse -> null);
            }
        });
    }

    private CompletionStage<WSResponse> deleteBulk(List<DeleteRequest> deleteRequests) {
        List<JsonNode> jsonNodes = deleteRequests.stream().map(Json::toJson).collect(Collectors.toList());
        String content = StringUtils.join(jsonNodes, "\n") + "\n";
        Logger.debug(content);
        return ws.url(baseUrl + BULK_ENDPOINT).post(content);
    }

    public CompletionStage<BulkResponse> postBulk(List<IndexRequest> indexRequestList) {
        List<JsonNode> jsonNodes = new ArrayList<>();
        for (IndexRequest indexRequest : indexRequestList) {
            jsonNodes.add(Json.toJson(indexRequest.getAction()));
            jsonNodes.add(indexRequest.getSource());
        }

        String content = StringUtils.join(jsonNodes, "\n") + "\n";
        Logger.debug(content);

        return ws.url(baseUrl + BULK_ENDPOINT).setContentType("application/x-www-form-urlencoded").post(content).thenApply(
                response -> {
                    Logger.debug(response.getBody());
                    return Json.fromJson(response.asJson(), BulkResponse.class);
                }
        );
    }

    private CompletionStage<SearchResponse> getOldDataPoints() {
        SearchBodyQuery bodyQuery = new SearchBodyQuery();
        SearchRange range = new SearchRange();
        SearchTimestamp timestamp = new SearchTimestamp();
        Time time = new Time();
        long startDeletingDate = time.daysAgo(30).toDate().getTime();
        timestamp.setLte(startDeletingDate);
        range.setTimestamp(timestamp);
        bodyQuery.setRange(range);
        SearchBody searchBody = new SearchBody();
        searchBody.setSize(10000);
        searchBody.setQuery(bodyQuery);
        String content = StringUtils.join(Json.toJson(searchBody), "\n", "\n");
        return ws.url(baseUrl + SEARCH_ENDPOINT).setContentType("application/x-www-form-urlencoded").post(content).thenApply(
                response -> {
                    Logger.debug(response.getBody());
                    return Json.fromJson(response.asJson(), SearchResponse.class);
                }
        );
    }

    public CompletionStage<MSearchResponse> multiSearch(List<SearchQuery> indexRequestList) {
        List<JsonNode> jsonNodes = new ArrayList<>();
        for (SearchQuery indexRequest : indexRequestList) {
            jsonNodes.add(Json.toJson(indexRequest.getSearchIndex()));
            jsonNodes.add(Json.toJson(indexRequest.getSearchBody()));
        }

        String content = StringUtils.join(jsonNodes, "\n") + "\n";

        Logger.debug(content);

        return ws.url(baseUrl + MSEARCH_ENDPOINT).setContentType("application/x-www-form-urlencoded").post(content).thenApply(
                response -> {
                    Logger.debug(response.getBody());
                    return Json.fromJson(response.asJson(), MSearchResponse.class);
                }
        );
    }

    private List<DeleteRequest> mapToDeleteRequests(Hits hits) {
        List<DeleteRequest> deletes = new LinkedList<>();
        for (JsonNode hit : hits.getHits()) {
            String index = hit.get("_index").asText();
            String type = hit.get("_type").asText();
            String id = hit.get("_id").asText();
            deletes.add(new DeleteRequest(index, type, id));
        }
        return deletes;
    }

}
