package usecases;

import datasources.elasticsearch.ElasticSearchDatasource;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class DeleteOldDataPoints {

    private final ElasticSearchDatasource elasticsearch;

    @Inject
    public DeleteOldDataPoints(ElasticSearchDatasource elasticsearch) {
        this.elasticsearch = elasticsearch;
    }

    public CompletionStage<Void> execute() {
        return elasticsearch.deleteOldDataPoints();
    }
}
