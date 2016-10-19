package datasources;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import play.Configuration;
import play.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ElasticsearchClient {

    private final TransportClient client;

    @Inject
    public ElasticsearchClient(@Named("elasticsearch") Configuration elasticsearchConf) throws UnknownHostException {
        String scheme = elasticsearchConf.getString("scheme");
        String host = elasticsearchConf.getString("host");
        String port = elasticsearchConf.getString("port");

        this.client = TransportClient.builder().build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), Integer.parseInt(port)));
    }

    public CompletionStage<BulkResponse> postBulk(List<IndexRequest> indexRequestList) {
        Logger.debug(StringUtils.join(indexRequestList, "\n"));

        BulkRequestBuilder bulkRequest = client.prepareBulk();
        indexRequestList.forEach(bulkRequest::add);

        return CompletableFuture.supplyAsync(bulkRequest::get)
                .thenApply(response -> {
                    Logger.debug(StringUtils.join(response.getItems(), "\n"));
                    return response;
        });
    }
}
