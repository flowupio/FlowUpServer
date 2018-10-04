package datasources.elasticsearch;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MatchAggregation {

    private Map<String, String> term = new HashMap<>();

    public void put(String field, String value) {
        term.put(field, value);
    }
}
