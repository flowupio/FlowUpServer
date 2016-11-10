package datasources.elasticsearch;

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
    private String searchType;
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
    private QueryString queryString;
}

@Data
class QueryString {
    private boolean analyzeWildcard;
    private String query;
}