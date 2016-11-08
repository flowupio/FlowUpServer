package usecases;

import lombok.Data;

@Data
public class ApiKeyConfig {

    private final boolean enabled;

    public ApiKeyConfig(boolean enabled) {
        this.enabled = enabled;
    }
}
