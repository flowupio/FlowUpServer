package datasources.elasticsearch;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import play.Configuration;
import play.Logger;
import play.libs.Json;
import play.libs.ws.WSClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class ElasticsearchClient {

    private static final String BULK_ENDPOINT = "/_bulk";
    private static final String MSEARCH_ENDPOINT = "/_msearch";

    private final WSClient ws;
    private final Configuration elasticsearchConf;
    private final String baseUrl;

    @Inject
    public ElasticsearchClient(WSClient ws, @Named("elasticsearch") Configuration elasticsearchConf) {
        this.ws = ws;
        this.elasticsearchConf = elasticsearchConf;

        String scheme = elasticsearchConf.getString("scheme");
        String host = elasticsearchConf.getString("host");
        String port = elasticsearchConf.getString("port");
        this.baseUrl = scheme + "://" + host + ":" + port;
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

    public CompletionStage<MSearchResponse> multiSearch(List<SearchQuery> indexRequestList) {

        List<JsonNode> jsonNodes = new ArrayList<>();
        for (SearchQuery indexRequest : indexRequestList) {
            jsonNodes.add(Json.toJson(indexRequest.getSearchIndex()));
            jsonNodes.add(Json.toJson(indexRequest.getSearchBody()));
        }

        String content = StringUtils.join(jsonNodes, "\n") + "\n";

        Logger.debug(content);

        return ws.url(baseUrl + BULK_ENDPOINT).setContentType("application/x-www-form-urlencoded").post(content).thenApply(
                response -> {
                    Logger.debug(response.getBody());
                    return Json.fromJson(response.asJson(), MSearchResponse.class);
                }
        );
    }

}
