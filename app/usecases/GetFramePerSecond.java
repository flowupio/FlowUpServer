package usecases;

import datasources.elasticsearch.ElasticSearchDatasource;
import datasources.elasticsearch.MSearchResponse;
import models.Application;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class GetFramePerSecond {
    private final ElasticSearchDatasource elasticSearchDatasource;

    @Inject
    public GetFramePerSecond(ElasticSearchDatasource elasticSearchDatasource) {
        this.elasticSearchDatasource = elasticSearchDatasource;
    }

    public CompletionStage<MSearchResponse> execute(Application application) {
        return elasticSearchDatasource.singleStat(application);
    }
}
