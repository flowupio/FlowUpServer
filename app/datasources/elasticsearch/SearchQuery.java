package datasources.elasticsearch;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SearchQuery {
    private SearchIndex searchIndex;
    private SearchBody searchBody;
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

