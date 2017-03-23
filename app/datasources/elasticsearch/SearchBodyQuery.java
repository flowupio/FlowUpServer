package datasources.elasticsearch;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
public class SearchBodyQuery {
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    private SearchBodyQueryFiltered filtered;
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    private SearchRange range;

}