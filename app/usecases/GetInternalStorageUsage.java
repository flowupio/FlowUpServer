package usecases;

import com.google.inject.Inject;
import models.Application;
import usecases.models.KeyStatCard;
import usecases.models.Threshold;
import usecases.models.Unit;

import java.util.concurrent.CompletionStage;

public class GetInternalStorageUsage extends GetLineChart {

    private static final String INTERNAL_STORAGE_WRITTEN_BYTES = "InternalStorageWrittenBytes";
    private static final String INTERNAL_STORAGE = "Internal Storage";

    @Inject
    protected GetInternalStorageUsage(MetricsDatasource metricsDatasource) {
        super(metricsDatasource);
    }

    public CompletionStage<KeyStatCard> execute(Application application) {
        return super.execute(application, INTERNAL_STORAGE_WRITTEN_BYTES, INTERNAL_STORAGE, Unit.BYTE);
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
