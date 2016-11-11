package usecases;

import datasources.elasticsearch.ElasticSearchDatasource;
import models.Application;
import usecases.models.LineChart;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

class GetLineChart {
    private final ElasticSearchDatasource elasticSearchDatasource;

    @Inject
    GetLineChart(ElasticSearchDatasource elasticSearchDatasource) {
        this.elasticSearchDatasource = elasticSearchDatasource;
    }

    CompletionStage<LineChart> executeSingleStat(Application application, String field) {
        return elasticSearchDatasource.singleStat(application, field);
    }

    CompletionStage<LineChart> executeSingleStat(Application application, String field, String queryStringValue) {
        return elasticSearchDatasource.singleStat(application, field, queryStringValue);
    }
}
