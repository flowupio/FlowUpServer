package datasources.elasticsearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
public class SearchBody {
    private long size;
    private SearchBodyQuery query;
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    private AggregationMap aggs;
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    private JsonNode filter;
}
