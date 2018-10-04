package datasources.elasticsearch;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SearchIndex {
    private String index;
    @JsonProperty("search_type")
    private String searchType;
    @JsonProperty("ignore_unavailable")
    private boolean ignoreUnavailable;
}