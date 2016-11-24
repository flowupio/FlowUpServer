package controllers.api;

interface DatapointTags {
    String getAppVersionName();

    String getAndroidOSVersion();

    boolean isBatterySaverOn();
}
