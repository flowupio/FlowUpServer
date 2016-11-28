package usecases;

import com.google.inject.Inject;
import models.Application;
import usecases.models.KeyStatCard;
import usecases.models.Threshold;
import usecases.models.Unit;

import java.util.concurrent.CompletionStage;

public class GetMemoryUsage extends GetLineChart {

    private static final String CONSUMPTION = "Consumption";
    private static final String TYPE_MEMORY_DATA = "_type:memory_data";
    private static final String MEMORY_USAGE = "Memory Usage";

    @Inject
    protected GetMemoryUsage(MetricsDatasource metricsDatasource) {
        super(metricsDatasource);
    }

    public CompletionStage<KeyStatCard> execute(Application application) {
        return super.execute(application, CONSUMPTION, TYPE_MEMORY_DATA, MEMORY_USAGE, Unit.PERCENTAGE);
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
