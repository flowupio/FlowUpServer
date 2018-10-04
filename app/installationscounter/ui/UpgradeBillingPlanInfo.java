package installationscounter.ui;

public class UpgradeBillingPlanInfo {

    private final long numberOfAllowedUUIDs;
    private final long currentDevices;

    public UpgradeBillingPlanInfo(long numberOfAllowedUUIDs, long currentDevices) {
        this.numberOfAllowedUUIDs = numberOfAllowedUUIDs;
        this.currentDevices = currentDevices;
    }

    public long getNumberOfAllowedUUIDs() {
        return numberOfAllowedUUIDs;
    }

    public long getCurrentDevices() {
        return currentDevices;
    }

    public boolean shouldShowMessage() {
        return currentDevices >= numberOfAllowedUUIDs;
    }

}
