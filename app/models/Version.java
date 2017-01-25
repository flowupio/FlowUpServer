package models;

import lombok.Data;
import org.jetbrains.annotations.NotNull;
import play.Logger;

@Data
public class Version implements Comparable<Version> {

    public static final Version UNKNOWN_VERSION = new Version(0, 0, 0, Platform.UNKNOWN);

    private final int major;
    private final int minor;
    private final int patch;
    private final Platform platform;

    public static Version fromString(String value) {
        if (value == null) {
            return UNKNOWN_VERSION;
        }
        try {
            Platform platform = value.startsWith("FlowUpAndroidSDK") ? Platform.ANDROID : Platform.IOS;
            int startIndex = value.indexOf('/') + 1;
            int lastIndex = value.indexOf("-");
            int endIndex = lastIndex == -1 ? value.length() : lastIndex;
            String[] versions = value.substring(startIndex, endIndex).split("\\.");
            int major = Integer.parseInt(versions[0]);
            int minor = Integer.parseInt(versions[1]);
            int patch = Integer.parseInt(versions[2]);
            return new Version(major, minor, patch, platform);
        } catch (Throwable e) {
            Logger.warn("Error parsing FlowUp user agent information", e);
            return UNKNOWN_VERSION;
        }
    }

    @Override
    public int compareTo(@NotNull Version o) {
        if (this.platform != o.platform) {
            return -1;
        } else if (getVersionNumber() > o.getVersionNumber()) {
            return 1;
        } else if (getVersionNumber() < o.getVersionNumber()) {
            return -1;
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        String clientName = platform == Platform.ANDROID ? "FlowUpAndroidSDK" : "FlowUpIOSSDK";
        return clientName + "/" + getMajor() + "." + getMinor() + "." + getPatch();
    }

    private int getVersionNumber() {
        return (getMajor() * 10000) + (getMinor() * 100 + getPatch());
    }


}
