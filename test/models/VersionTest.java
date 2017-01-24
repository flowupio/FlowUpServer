package models;

import org.junit.Test;

import static org.junit.Assert.*;

public class VersionTest {

    @Test
    public void parsesARegularAndroidUserAgentHeader() {
        Version version = Version.fromString("FlowUpAndroidSDK/0.2.7");

        assertEquals(new Version(0,2,7, Platform.ANDROID), version);
    }

    @Test
    public void parsesASnapshotVersionOfTheAndroidClient() {
        Version version = Version.fromString("FlowUpAndroidSDK/0.2.8-SNAPSHOT");

        assertEquals(new Version(0,2,8, Platform.ANDROID), version);
    }

    @Test
    public void parsesADebugVersionOfTheAndroidClient() {
        Version version = Version.fromString("FlowUpAndroidSDK/0.2.7-DEBUG");

        assertEquals(new Version(0,2,7, Platform.ANDROID), version);
    }

    @Test
    public void parsesASnapshotAndDebugVersionOfTheAndroidClient() {
        Version version = Version.fromString("FlowUpAndroidSDK/0.2.8-SNAPSHOT-DEBUG");

        assertEquals(new Version(0,2,8, Platform.ANDROID), version);
    }

    @Test
    public void parsesAsUnknownVersionAnyMalformedUserAgentHeader() {
        Version version = Version.fromString("FlowUpAndroidSDK/DEBUG-0.2.8");

        assertEquals(Version.UNKNOWN_VERSION, version);
    }

}