package usecases.models;

import lombok.Data;

@Data
public class ErrorReport {

    private final String deviceModel;
    private final String osVersion;
    private final boolean batterySaverOn;
    private final String message;
    private final String stackTrace;
}
