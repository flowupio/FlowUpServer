package datasources.elasticsearch;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class SearchTimestamp {
    private Long lte;
    private Long gte;
}