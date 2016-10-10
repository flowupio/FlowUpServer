package datasources;

import com.fasterxml.jackson.databind.JsonNode;
import play.Configuration;
import play.libs.ws.WSClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.CompletionStage;

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

    public CompletionStage<JsonNode> postBulk(String content) {
        String bulkEndpoint = elasticsearchConf.getString("bulk_endpoint");

        return ws.url(baseUrl + bulkEndpoint).setContentType("application/x-www-form-urlencoded").post(content).thenApply(
                response ->
                        response.asJson()
        );
    }

}
