package usecases;

import com.google.inject.Inject;
import models.Application;
import usecases.models.KeyStatCard;
import usecases.models.Threshold;
import usecases.models.Unit;

import java.util.concurrent.CompletionStage;

public class GetCpuUsage extends GetLineChart {

    private static final String CONSUMPTION = "Consumption";
    private static final String TYPE_CPU_DATA = "_type:cpu_data";
    private static final String CPU_USAGE = "CPU Usage";

    @Inject
    protected GetCpuUsage(MetricsDatasource metricsDatasource) {
        super(metricsDatasource);
    }

    public CompletionStage<KeyStatCard> execute(Application application) {
        return super.execute(application, CONSUMPTION, TYPE_CPU_DATA, CPU_USAGE, Unit.PERCENTAGE);
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
