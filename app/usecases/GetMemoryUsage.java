package usecases;

import datasources.elasticsearch.ElasticSearchDatasource;
import models.Application;
import usecases.models.StatCard;
import usecases.models.Threshold;
import usecases.models.Unit;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class GetMemoryUsage extends GetLineChart {
    @Inject
    public GetMemoryUsage(ElasticSearchDatasource elasticSearchDatasource) {
        super(elasticSearchDatasource);
    }

    public CompletionStage<StatCard> execute(Application application) {
        return super.execute(application, "Consumption", "_type:memory_data", "Memory Usage", Unit.PERCENTAGE);
    }

    @Override
    Threshold getThreshold(Double average) {
        Threshold threshold;
        if (average != null) {
            if (average < 50) {
                threshold = Threshold.OK;
            } else if (average < 75) {
                threshold = Threshold.WARNING;
            } else {
                threshold = Threshold.SEVERE;
            }
        } else {
            threshold = Threshold.NO_DATA;
        }
        return threshold;
    }
}
