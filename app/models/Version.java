package models;

import lombok.Data;
import play.Logger;

@Data
public class Version {

    static final Version UNKNOWN_VERSION = new Version(0, 0, 0, Platform.UNKNOWN);

    private final int major;
    private final int minor;
    private final int patch;
    private final Platform platform;

    public static Version fromString(String value) {
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

}
