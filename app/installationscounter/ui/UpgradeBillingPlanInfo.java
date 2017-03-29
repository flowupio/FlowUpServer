package installationscounter.ui;

import lombok.Data;

@Data
public class UpgradeBillingPlanInfo {

    private final long numberOfAllowedUUIDs;
    private final long currentDevices;

    public boolean shouldShowMessage() {
        return currentDevices >= numberOfAllowedUUIDs;
    }

}
