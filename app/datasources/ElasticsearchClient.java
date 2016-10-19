package datasources;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import play.Configuration;
import play.Logger;
import play.libs.Json;
import play.libs.ws.WSClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;

public class ElasticsearchClient {

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
        Stream<JsonNode> jsonNodes = indexRequestList.stream().map(Json::toJson);
        String content = StringUtils.join(jsonNodes, "\n") + "\n";

        Logger.debug(content);

        String bulkEndpoint = elasticsearchConf.getString("bulk_endpoint");

        return ws.url(baseUrl + bulkEndpoint).setContentType("application/x-www-form-urlencoded").post(content).thenApply(
                response -> {
                    Logger.debug(response.getBody());
                    return Json.fromJson(response.asJson(), BulkResponse.class);
                }
        );
    }
}

