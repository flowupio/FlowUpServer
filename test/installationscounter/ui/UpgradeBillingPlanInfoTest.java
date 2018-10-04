package installationscounter.ui;


import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UpgradeBillingPlanInfoTest {

    @Test
    public void shouldShowUpgradeMessageIfTheNumberOfInstallationsIsGreaterThanTheNumberOfCurrentDevices() {
        UpgradeBillingPlanInfo upgradeBillingPlanInfo = new UpgradeBillingPlanInfo(2,4);

        assertTrue(upgradeBillingPlanInfo.shouldShowMessage());
    }

    @Test
    public void shouldShowUpgradeMessageIfTheNumberOfInstallationsIsEqualToTheNumberOfCurrentDevices() {
        UpgradeBillingPlanInfo upgradeBillingPlanInfo = new UpgradeBillingPlanInfo(2,2);

        assertTrue(upgradeBillingPlanInfo.shouldShowMessage());
    }

    @Test
    public void shouldNotShowUpgradeMessageIfTheNumberOfInstallationsIsGreaterThanTheNumberOfCurrentDevices() {
        UpgradeBillingPlanInfo upgradeBillingPlanInfo = new UpgradeBillingPlanInfo(4,2);

        assertFalse(upgradeBillingPlanInfo.shouldShowMessage());
    }

}