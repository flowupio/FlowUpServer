package usecases.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class ErrorReport {

    private final String deviceModel;
    private final String osVersion;
    private final boolean batterySaverOn;
    private final String message;
    private final String stackTrace;
    @JsonIgnore
    private String libraryVersion;
    @JsonIgnore
    private String apiKey;

}
