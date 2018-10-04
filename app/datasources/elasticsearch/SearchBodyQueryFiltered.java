package datasources.elasticsearch;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class SearchBodyQueryFiltered {
    private SearchBodyQueryFilteredQuery query;
    private JsonNode filter;
}