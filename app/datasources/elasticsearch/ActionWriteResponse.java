package datasources.elasticsearch;

import lombok.Data;
import org.jetbrains.annotations.Nullable;


@Data
public class ActionWriteResponse {
    protected final String index;
    @Nullable
    protected ShardInfo shardInfo;
    protected BulkError error;
    @Data
    public static class ShardInfo{
        private final int total;
        private final int successful;
    }
}
