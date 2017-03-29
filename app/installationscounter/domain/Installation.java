package installationscounter.domain;

import lombok.Data;
import models.Version;

@Data
public class Installation {

    private final String apiKey;
    private final String uuid;
    private final Version version;
    private final long timestamp;

}
