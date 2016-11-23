package usecases;

import com.google.inject.Inject;
import com.spotify.futures.CompletableFutures;
import models.Application;
import usecases.models.StatCard;

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

    public CompletionStage<List<StatCard>> execute(Application application) {
        CompletableFuture<StatCard> framePerSecondCompletionStage = getFramePerSecond.execute(application).toCompletableFuture();
        CompletableFuture<StatCard> internalStorageUsageCompletionStage = getInternalStorageUsage.execute(application).toCompletableFuture();
        CompletableFuture<StatCard> cpuUsageCompletionStage = getCpuUsage.execute(application).toCompletableFuture();
        CompletableFuture<StatCard> memoryUsageCompletionStage = getMemoryUsage.execute(application).toCompletableFuture();

        List<CompletableFuture<StatCard>> futures = asList(framePerSecondCompletionStage, internalStorageUsageCompletionStage, cpuUsageCompletionStage, memoryUsageCompletionStage);
        return CompletableFutures.allAsList(futures);
    }
}
