package datasources.elasticsearch;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

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
    private JsonNode aggs;
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