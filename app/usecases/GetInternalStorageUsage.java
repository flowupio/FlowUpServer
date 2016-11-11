package usecases;

import datasources.elasticsearch.ElasticSearchDatasource;
import models.Application;
import usecases.models.StatCard;
import usecases.models.Threshold;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class GetInternalStorageUsage extends GetLineChart {
    @Inject
    public GetInternalStorageUsage(ElasticSearchDatasource elasticSearchDatasource) {
        super(elasticSearchDatasource);
    }

    public CompletionStage<StatCard> execute(Application application) {
        return super.execute(application, "InternalStorageWrittenBytes", "Internal Storage", "Kb");
    }

    @Override
    Threshold getThreshold(Double average) {
        Threshold threshold;
        if (average != null) {
            if (average < 104857600) {
                threshold = Threshold.OK;
            } else if (average < 209715200) {
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
