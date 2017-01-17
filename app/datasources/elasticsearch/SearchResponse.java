package datasources.elasticsearch;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
class SearchResponse {
    private Hits hits;
    private JsonNode aggregations;

    boolean hasHits() {
        return hits.getHits().size() > 0;
    }
}
