package datasources.elasticsearch;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TermsAggregation {
    private String field;
    private int size;
    private Map<String, String> order;
}