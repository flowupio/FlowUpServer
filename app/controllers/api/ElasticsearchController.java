package controllers.api;

import datasources.elasticsearch.ElasticSearchDatasource;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ElasticsearchController extends Controller {

    private final ElasticSearchDatasource elasticsearch;

    @Inject
    public ElasticsearchController(ElasticSearchDatasource elasticsearch) {
        this.elasticsearch = elasticsearch;
    }

    //@BodyParser.Of(SNSMessageBodyParser.class)
    public CompletionStage<Result> deleteOldDataPoints() {
        return CompletableFuture.supplyAsync(() -> {
            elasticsearch.deleteOldDataPoints();
            return ok();
        });
    }
}
