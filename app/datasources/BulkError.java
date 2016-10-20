package datasources;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BulkError {
    @JsonProperty("root_cause")
    private List<RootCause> rootCause = new ArrayList<>();
    private String type;
    private String reason;

    @Data
    public static class RootCause {
        private String type;
        private String reason;
    }
}
