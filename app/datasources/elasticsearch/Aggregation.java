package datasources.elasticsearch;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Aggregation {
    @JsonProperty("date_histogram")
    private DateHistogramAggregation dateHistogram;
    private AvgAggregation avg;
    private TermsAggregation terms;
    private AggregationMap aggs;
}