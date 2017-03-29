package datasources.elasticsearch;

import lombok.Data;

@Data
public class SearchResponse {
    private Hits hits;
    private SearchAggregations aggregations;
}
