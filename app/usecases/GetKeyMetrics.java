package usecases;

import com.google.inject.Inject;
import com.spotify.futures.CompletableFutures;
import models.Application;
import usecases.models.KeyStatCard;
import usecases.models.StatCard;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static java.util.Arrays.asList;

public class GetKeyMetrics {
    private final GetFramePerSecond getFramePerSecond;
    private final GetInternalStorageUsage getInternalStorageUsage;
    private final GetCpuUsage getCpuUsage;
    private final GetMemoryUsage getMemoryUsage;

    @Inject
    public GetKeyMetrics(GetFramePerSecond getFramePerSecond, GetInternalStorageUsage getInternalStorageUsage, GetCpuUsage getCpuUsage, GetMemoryUsage getMemoryUsage) {
        this.getFramePerSecond = getFramePerSecond;
        this.getInternalStorageUsage = getInternalStorageUsage;
        this.getCpuUsage = getCpuUsage;
        this.getMemoryUsage = getMemoryUsage;
    }

    public CompletionStage<List<KeyStatCard>> execute(Application application) {
        CompletableFuture<KeyStatCard> framePerSecondCompletionStage = getFramePerSecond.execute(application).toCompletableFuture();
        CompletableFuture<KeyStatCard> internalStorageUsageCompletionStage = getInternalStorageUsage.execute(application).toCompletableFuture();
        CompletableFuture<KeyStatCard> cpuUsageCompletionStage = getCpuUsage.execute(application).toCompletableFuture();
        CompletableFuture<KeyStatCard> memoryUsageCompletionStage = getMemoryUsage.execute(application).toCompletableFuture();

        List<CompletableFuture<KeyStatCard>> futures = asList(framePerSecondCompletionStage, internalStorageUsageCompletionStage, cpuUsageCompletionStage, memoryUsageCompletionStage);
        return CompletableFutures.allAsList(futures);
    }
}
