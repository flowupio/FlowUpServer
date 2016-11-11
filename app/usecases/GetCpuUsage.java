package usecases;

import datasources.elasticsearch.ElasticSearchDatasource;
import models.Application;
import org.jetbrains.annotations.NotNull;
import usecases.models.StatCard;
import usecases.models.Threshold;

import javax.inject.Inject;
import java.util.OptionalDouble;
import java.util.concurrent.CompletionStage;

public class GetCpuUsage extends GetLineChart {
    @Inject
    public GetCpuUsage(ElasticSearchDatasource elasticSearchDatasource) {
        super(elasticSearchDatasource);
    }

    public CompletionStage<StatCard> execute(Application application) {
        return super.execute(application, "Consumption", "_type:cpu_data", "CPU Usage", "%");
    }

    @Override
    Threshold getThreshold(Double average) {
        Threshold threshold;
        if (average != null) {
            if (average < 20) {
                threshold = Threshold.OK;
            } else if (average < 50) {
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
