package installationscounter.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import models.Version;

@Data
public class Installation {

    private final String apiKey;
    private final String uuid;
    private final Version version;
    @JsonProperty("@timestamp")
    private final long timestamp;

}
