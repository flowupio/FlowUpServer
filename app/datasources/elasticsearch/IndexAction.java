package datasources.elasticsearch;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class IndexAction implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("index")
    private final Metadata index;

    @Data
    class Metadata implements Serializable {
        private static final long serialVersionUID = 1L;

        @JsonProperty("_index")
        private final String index;
        @JsonProperty("_type")
        private final String type;
    }

    IndexAction(String index, String type) {
        this.index = new Metadata(index, type);
    }
}
