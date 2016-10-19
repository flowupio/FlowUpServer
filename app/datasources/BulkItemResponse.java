package datasources;

import lombok.Data;

@Data
public class BulkItemResponse {
    private final ActionWriteResponse response;
    private String error;
    private final String opType;
    private final int id;

    public BulkItemResponse(int id, String opType, ActionWriteResponse response) {
        this.id = id;
        this.opType = opType;
        this.response = response;
    }

    boolean isFailed() {
        return error != null && !error.isEmpty();
    }

    public String getIndex() {
        return response.getIndex();
    }
}
