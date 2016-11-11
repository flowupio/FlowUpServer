package datasources.elasticsearch;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.List;

@Data
public class Hits {
    private long total;
    private double maxScore;
    private List<JsonNode> hits;
}

