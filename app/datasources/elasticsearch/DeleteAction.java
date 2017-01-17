package datasources.elasticsearch;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

public class DeleteAction {

    @JsonProperty("delete")
    private final DeleteAction.Metadata index;

    @Data
    static class Metadata {

        @JsonProperty("_index")
        private final String index;
        @JsonProperty("_type")
        private final String type;
        @JsonPropertyOrder("_id")
        private final String id;
    }

    DeleteAction(String index, String type, String id) {
        this.index = new DeleteAction.Metadata(index, type, id);
    }
}
