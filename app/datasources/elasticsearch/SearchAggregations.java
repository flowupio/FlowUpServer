package datasources.elasticsearch;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SearchAggregations {

    @JsonProperty("group_by_state")
    private final SearchGroup groupByState;
}
