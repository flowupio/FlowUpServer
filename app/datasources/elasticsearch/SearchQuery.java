package datasources.elasticsearch;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

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
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
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
    private TermsAggregation terms;
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
class TermsAggregation {
    private String field;
    private int size;
    private Map<String, String> order;
}

@Data
class ExtendedBounds {
    private final long min;
    private final long max;
}

@Data
class SearchBodyQuery {
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    private SearchBodyQueryFiltered filtered;
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    private SearchRange range;

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

@Data
class SearchRange {
    @JsonProperty("@timestamp")
    SearchTimestamp timestamp;
}

@Data
class SearchTimestamp {
    private long lte;
}
