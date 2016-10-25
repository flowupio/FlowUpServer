package datasources.elasticsearch;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class IndexAction {
    @JsonProperty("index")
    private final Metadata index;

    @Data
    class Metadata {
        @JsonProperty("_index")
        private final String index;
        @JsonProperty("_type")
        private final String type;
    }

    IndexAction(String index, String type) {
        this.index = new Metadata(index, type);
    }
}
