package datasources.elasticsearch;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.HashMap;

@Data
public class SearchQuery {
    private SearchIndex searchIndex;
    private SearchBody searchBody;
}

@Data
class SearchIndex {
    private String index;
    @JsonProperty("search_type")
    private String searchType;
    @JsonProperty("ignore_unavailable")
    private boolean ignoreUnavailable;
}

@Data
class SearchBody {
    private long size;
    private SearchBodyQuery query;
    private AggregationMap aggs;
}

class AggregationMap extends HashMap<String, Aggregation> {
    static AggregationMap singleton(String name, Aggregation aggsObject) {
        AggregationMap aggs = new AggregationMap();
        aggs.put(name, aggsObject);
        return aggs;
    }
}

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
class Aggregation {
    @JsonProperty("date_histogram")
    private DateHistogramAggregation dateHistogram;
    private AvgAggregation avg;
    private AggregationMap aggs;
}

@Data
class AvgAggregation {
    private final String field;
}

@Data
class DateHistogramAggregation {
    private final String interval;
    private final String field;
    @JsonProperty("min_doc_count")
    private final int minDocCount;
    private final String format;
    @JsonProperty("extended_bounds")
    private final ExtendedBounds extendedBounds;
}

@Data
class ExtendedBounds {
    private final long min;
    private final long max;
}

@Data
class SearchBodyQuery {
    private SearchBodyQueryFiltered filtered;

}

@Data
class SearchBodyQueryFiltered {
    private SearchBodyQueryFilteredQuery query;
    private JsonNode filter;
}

@Data
class SearchBodyQueryFilteredQuery {
    @JsonProperty("query_string")
    private QueryString queryString;
}

@Data
class QueryString {
    @JsonProperty("analyze_wildcard")
    private boolean analyzeWildcard;
    private String query;
}