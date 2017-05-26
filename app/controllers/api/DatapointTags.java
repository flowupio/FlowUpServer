package controllers.api;

interface DatapointTags {
    String getAppVersionName();

    String getAndroidOSVersion();

    String getIOSVersion();

    boolean isBatterySaverOn();
}
