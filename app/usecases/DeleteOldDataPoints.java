package usecases;

import datasources.elasticsearch.ElasticSearchDatasource;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class DeleteOldDataPoints {

    private final ElasticSearchDatasource elasticsearch;

    @Inject
    public DeleteOldDataPoints(ElasticSearchDatasource elasticsearch) {
        this.elasticsearch = elasticsearch;
    }

    public CompletionStage<Void> execute() {
        return elasticsearch.deleteOldDataPoints()
                .thenCompose(result -> elasticsearch.deleteEmptyIndexes());
    }
}
