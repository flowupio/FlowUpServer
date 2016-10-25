package datasources.elasticsearch;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class IndexRequest {
    private final IndexAction action;
    private JsonNode source;

    public IndexRequest(String index, String type) {
        this.action = new IndexAction(index, type);
    }
}
