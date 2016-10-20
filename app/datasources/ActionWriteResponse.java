package datasources;

import lombok.Data;

@Data
public class ActionWriteResponse {
    protected final String index;
    protected ShardInfo shardInfo;
    @Data
    public static class ShardInfo{
        private final int total;
        private final int successful;
    }
}
