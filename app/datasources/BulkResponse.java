package datasources;

import lombok.Data;

@Data
public class BulkResponse {
    private final BulkItemResponse[] items;
    private final long tookInMillis;

    boolean hasFailures() {
        for (BulkItemResponse response : items) {
            if (response.isFailed()) {
                return true;
            }
        }
        return false;
    }
}
