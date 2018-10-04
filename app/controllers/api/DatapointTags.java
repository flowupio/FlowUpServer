package controllers.api;

interface DatapointTags {
    String getAppVersionName();

    String getAndroidOSVersion();

    String getIOSVersion();

    boolean isBatterySaverOn();

    default boolean isAndroid() {
        return getAndroidOSVersion() != null;
    }

    default boolean isIos() {
        return getIOSVersion() != null;
    }
}
