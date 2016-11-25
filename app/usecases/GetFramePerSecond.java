package usecases;

import com.google.inject.Inject;
import models.Application;
import usecases.models.KeyStatCard;
import usecases.models.StatCard;
import usecases.models.Threshold;
import usecases.models.Unit;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class GetFramePerSecond extends GetLineChart {
    private static final String FRAMES_PER_SECOND_P90 = "FramesPerSecond.p90";
    private static final String FRAMES_PER_SECOND = "Frames per second";

    @Inject
    protected GetFramePerSecond(MetricsDatasource metricsDatasource) {
        super(metricsDatasource);
    }

    public CompletionStage<KeyStatCard> execute(Application application) {
        return super.execute(application, FRAMES_PER_SECOND_P90, FRAMES_PER_SECOND, Unit.NONE);
    }

    @Override
    Threshold getThreshold(Double average) {
        Threshold threshold;
        if (average != null) {
            if (average > 50) {
                threshold = Threshold.OK;
            } else if (average > 40) {
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
