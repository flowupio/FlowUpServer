package datasources.elasticsearch;

import lombok.Data;

@Data
public class BulkResponse {
    private BulkItemResponse[] items = {};
    private long tookInMillis;
    private BulkError error;
    private boolean errors;

    public BulkResponse() {
    }

    public BulkResponse(BulkItemResponse[] items, long tookInMillis) {
        this.items = items;
        this.tookInMillis = tookInMillis;
    }

    boolean hasFailures() {
        for (BulkItemResponse response : items) {
            if (response.isFailed()) {
                return true;
            }
        }
        return false;
    }

    boolean isError() {
        return error != null;
    }
}
