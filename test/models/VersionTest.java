package models;

import org.junit.Test;

import static org.junit.Assert.*;

public class VersionTest {

    @Test
    public void parsesARegularAndroidUserAgentHeader() {
        Version version = Version.fromString("FlowUpAndroidSDK/0.2.7");

        assertEquals(new Version(0, 2, 7, Platform.ANDROID), version);
    }

    @Test
    public void parsesASnapshotVersionOfTheAndroidClient() {
        Version version = Version.fromString("FlowUpAndroidSDK/0.2.8-SNAPSHOT");

        assertEquals(new Version(0, 2, 8, Platform.ANDROID), version);
    }

    @Test
    public void parsesADebugVersionOfTheAndroidClient() {
        Version version = Version.fromString("FlowUpAndroidSDK/0.2.7-DEBUG");

        assertEquals(new Version(0, 2, 7, Platform.ANDROID, true), version);
    }

    @Test
    public void parsesASnapshotAndDebugVersionOfTheAndroidClient() {
        Version version = Version.fromString("FlowUpAndroidSDK/0.2.8-SNAPSHOT-DEBUG");

        assertEquals(new Version(0, 2, 8, Platform.ANDROID, true), version);
    }

    @Test
    public void parsesAsUnknownVersionAnyMalformedUserAgentHeader() {
        Version version = Version.fromString("FlowUpAndroidSDK/DEBUG-0.2.8");

        assertEquals(Version.UNKNOWN_VERSION, version);
    }

    @Test
    public void versionsWithDifferentPlatformsShouldBeSmaller() {
        Version androidVersion = new Version(0, 1, 3, Platform.ANDROID);
        Version iOSVersion = new Version(0, 1, 3, Platform.IOS);

        assertEquals(-1, androidVersion.compareTo(iOSVersion));
    }

    @Test
    public void versionsWithALowerPatchValueShouldBeSmaller() {
        Version version = new Version(0, 1, 2, Platform.ANDROID);
        Version otherVersion = new Version(0, 1, 3, Platform.ANDROID);

        assertEquals(-1, version.compareTo(otherVersion));
    }

    @Test
    public void versionsWithALowerMinorValueShouldBeSmaller() {
        Version version = new Version(0, 1, 3, Platform.ANDROID);
        Version otherVersion = new Version(0, 2, 3, Platform.ANDROID);

        assertEquals(-1, version.compareTo(otherVersion));
    }

    @Test
    public void versionsWithALowerMajorValueShouldBeSmaller() {
        Version version = new Version(0, 1, 3, Platform.ANDROID);
        Version otherVersion = new Version(1, 1, 3, Platform.ANDROID);

        assertEquals(-1, version.compareTo(otherVersion));
    }

    @Test
    public void versionsWithTheSameValuesAndPlatformsShouldBeEqual() {
        Version version = new Version(0, 1, 3, Platform.ANDROID);
        Version otherVersion = new Version(0, 1, 3, Platform.ANDROID);

        assertEquals(0, version.compareTo(otherVersion));
    }

    @Test
    public void versionsWithABiggerPatchValueShouldBeGreater() {
        Version version = new Version(0, 1, 3, Platform.ANDROID);
        Version otherVersion = new Version(0, 1, 2, Platform.ANDROID);

        assertEquals(1, version.compareTo(otherVersion));
    }

    @Test
    public void versionsWithABiggerMinorValueShouldBeGreater() {
        Version version = new Version(0, 2, 3, Platform.ANDROID);
        Version otherVersion = new Version(0, 1, 3, Platform.ANDROID);

        assertEquals(1, version.compareTo(otherVersion));
    }

    @Test
    public void versionsWithABiggerMajorValueShouldBeGreater() {
        Version version = new Version(1, 1, 3, Platform.ANDROID);
        Version otherVersion = new Version(0, 1, 3, Platform.ANDROID);

        assertEquals(1, version.compareTo(otherVersion));
    }

}