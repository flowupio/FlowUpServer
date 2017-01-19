package datasources.elasticsearch;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import play.Configuration;
import play.Logger;
import play.libs.Json;
import play.libs.ws.WSClient;
import utils.Time;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class ElasticsearchClient {

    private static final String BULK_ENDPOINT = "/_bulk";
    private static final String MSEARCH_ENDPOINT = "/_msearch";
    private static final String SEARCH_ENDPOINT = "/_search";
    private static final String ELASTIC_CONTENT_TYPE = "application/x-www-form-urlencoded";
    private static final String INDEXES_ENDPOINT = "/_cat/indices?v";
    private static final int MIN_DOCUMENTS_TTL = 30;
    private static final int MAX_ELASTICSEARCH_QUERY_SIZE = 10000;

    private final WSClient ws;
    private final String baseUrl;
    private final int documentsTTL;
    private final IndexParser indexParser;
    private final Time time;

    @Inject
    public ElasticsearchClient(WSClient ws, @Named("elasticsearch") Configuration elasticsearchConf, Time time) {
        this.ws = ws;
        String scheme = elasticsearchConf.getString("scheme");
        String host = elasticsearchConf.getString("host");
        String port = elasticsearchConf.getString("port");
        this.baseUrl = scheme + "://" + host + ":" + port;
        this.documentsTTL = Math.max(MIN_DOCUMENTS_TTL, elasticsearchConf.getInt("documents_ttl_in_days"));
        this.indexParser = new IndexParser();
        this.time = time;
    }

    public CompletionStage<BulkResponse> postBulk(List<IndexRequest> indexRequestList) {
        List<JsonNode> jsonNodes = new ArrayList<>();
        for (IndexRequest indexRequest : indexRequestList) {
            jsonNodes.add(Json.toJson(indexRequest.getAction()));
            jsonNodes.add(indexRequest.getSource());
        }

        String content = StringUtils.join(jsonNodes, "\n") + "\n";
        Logger.debug(content);

        return ws.url(baseUrl + BULK_ENDPOINT).setContentType(ELASTIC_CONTENT_TYPE).post(content).thenApply(
                response -> {
                    Logger.debug(response.getBody());
                    return Json.fromJson(response.asJson(), BulkResponse.class);
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

        return ws.url(baseUrl + MSEARCH_ENDPOINT).setContentType(ELASTIC_CONTENT_TYPE).post(content).thenApply(
                response -> {
                    Logger.debug(response.getBody());
                    return Json.fromJson(response.asJson(), MSearchResponse.class);
                }
        );
    }

    public CompletionStage<Void> deleteBulk(List<DeleteRequest> deleteRequests) {
        List<JsonNode> jsonNodes = deleteRequests.stream().map(Json::toJson).collect(Collectors.toList());
        String content = StringUtils.join(jsonNodes, "\n") + "\n";
        Logger.debug(content);
        return ws.url(baseUrl + BULK_ENDPOINT).post(content).thenApply(response -> null);
    }

    public CompletionStage<SearchResponse> getOldDataPoints() {
        SearchBodyQuery bodyQuery = new SearchBodyQuery();
        SearchRange range = new SearchRange();
        SearchTimestamp timestamp = new SearchTimestamp();
        long startDeletingDate = time.daysAgo(documentsTTL).toDate().getTime();
        timestamp.setLte(startDeletingDate);
        range.setTimestamp(timestamp);
        bodyQuery.setRange(range);
        SearchBody searchBody = new SearchBody();
        searchBody.setSize(MAX_ELASTICSEARCH_QUERY_SIZE);
        searchBody.setQuery(bodyQuery);
        return search(searchBody);
    }

    public CompletionStage<SearchResponse> search(SearchBody searchBody) {
        String content = StringUtils.join(Json.toJson(searchBody), "\n", "\n");
        Logger.debug("--------> " + content);
        return ws.url(baseUrl + SEARCH_ENDPOINT).setContentType(ELASTIC_CONTENT_TYPE).post(content).thenApply(
                response -> {
                    Logger.debug(response.getBody());
                    return Json.fromJson(response.asJson(), SearchResponse.class);
                }
        );
    }

    public CompletionStage<List<Index>> getIndexes() {
        return ws.url(baseUrl + INDEXES_ENDPOINT).setContentType(ELASTIC_CONTENT_TYPE).get().thenApply(
                response -> {
                    Logger.debug(response.getBody());
                    return indexParser.toIndexes(response.getBody());
                }
        );
    }

    public CompletionStage<Void> deleteIndex(String indexName) {
        return ws.url(baseUrl + "/" + indexName).setContentType(ELASTIC_CONTENT_TYPE).delete().thenApply(
                response -> {
                    Logger.debug(response.getBody());
                    return null;
                }
        );
    }
}
