package usecases.repositories;

import io.netty.handler.codec.http.HttpResponseStatus;
import models.Platform;
import play.Logger;
import play.cache.CacheApi;
import play.libs.Json;
import play.libs.ws.WSClient;
import usecases.models.AndroidSDKVersion;
import usecases.models.IOSSDKVersion;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public class SDKVersionNameRepository {

    private static final int SDK_VERSION_TTL = (int) TimeUnit.DAYS.toSeconds(1);
    private static final String MAVEN_CENTRAL_SEARCH_PATH = "solrsearch/select";
    private static final String MAVEN_CENTRAL_BASE_URL = "http://search.maven.org/";
    private static final String MAVEN_CENTRAL_ARTIFACT_QUERY = "g:\"io.flowup\" AND a:\"android-sdk\"";
    private static final String MAVEN_CENTRAL_ARTIFACT_QUERY_KEY = "q";
    private static final String COCOAPODS_SEARCH_PATH = "api/pods";
    private static final String COCOAPODS_BASE_URL = "http://search.cocoapods.org/";
    private static final String COCOAPODS_ARTIFACT_QUERY = "FlowUpIOSSDK";
    private static final String COCOAPODS_ARTIFACT_QUERY_KEY = "query";
    private static final String COCOAPODS_ACCEPT_HEADER_KEY = "Accept";
    private static final String COCOAPODS_ACCEPT_HEADER_VALUE = "application/vnd.cocoapods.org+flat.hash.json; version=1";
    private static final String DEFAULT_VERSION_NAME = "<LATEST_VERSION>";
    static final String ANDROID_SDK_VERSION_CACHE_KEY = "androidSdkVersionName";
    static final String IOS_SDK_VERSION_CACHE_KEY = "iosSdkVersionName";

    private final CacheApi cache;
    private final WSClient ws;
    private final String mavenCentralBaseUrl;
    private final String cocoapodsBaseUrl;

    @Inject
    public SDKVersionNameRepository(CacheApi cache, WSClient ws) {
        this(cache, ws, MAVEN_CENTRAL_BASE_URL, COCOAPODS_BASE_URL);
    }

    public SDKVersionNameRepository(CacheApi cache, WSClient ws, String mavenCentralBaseUrl, String cocoapodsBaseUrl) {
        this.cache = cache;
        this.ws = ws;
        this.mavenCentralBaseUrl = mavenCentralBaseUrl + MAVEN_CENTRAL_SEARCH_PATH;
        this.cocoapodsBaseUrl = cocoapodsBaseUrl + COCOAPODS_SEARCH_PATH;
    }

    public CompletionStage<String> getLatestSDKVersionName(Platform platform) {
        String cachedLatestSDKVersion = cache.get(getCacheKey(platform));
        if (cachedLatestSDKVersion != null) {
            return CompletableFuture.completedFuture(cachedLatestSDKVersion);
        } else {
            CompletionStage<String> latestAndroidSDKVersionFromMavenCentral = getLatestSDKVersionFromAPI(platform);
            return latestAndroidSDKVersionFromMavenCentral.thenApply(s -> {
                cache.set(getCacheKey(platform), s, SDK_VERSION_TTL);
                return s;
            });
        }
    }

    private CompletionStage<String> getLatestSDKVersionFromAPI(Platform platform) {
        switch (platform) {
            case IOS:
                return getLatestIOSSDKVersionFromMavenCentral();
            case ANDROID:
                return getLatestAndroidSDKVersionFromMavenCentral();
            default:
                return CompletableFuture.completedFuture(DEFAULT_VERSION_NAME);
        }
    }

    private CompletionStage<String> getLatestIOSSDKVersionFromMavenCentral() {
        return ws.url(cocoapodsBaseUrl)
                .setQueryParameter(COCOAPODS_ARTIFACT_QUERY_KEY, COCOAPODS_ARTIFACT_QUERY)
                .setHeader(COCOAPODS_ACCEPT_HEADER_KEY, COCOAPODS_ACCEPT_HEADER_VALUE)
                .get()
                .thenApply(wsResponse -> {
                    if (wsResponse.getStatus() == HttpResponseStatus.OK.code()) {
                        IOSSDKVersion[] androidSDKVersion = Json.fromJson(wsResponse.asJson(), IOSSDKVersion[].class);
                        return androidSDKVersion.length > 0 ? androidSDKVersion[0].getVersion() : DEFAULT_VERSION_NAME;
                    } else {
                        return DEFAULT_VERSION_NAME;
                    }
                });
    }

    private CompletionStage<String> getLatestAndroidSDKVersionFromMavenCentral() {
        return ws.url(mavenCentralBaseUrl)
                .setQueryParameter(MAVEN_CENTRAL_ARTIFACT_QUERY_KEY, MAVEN_CENTRAL_ARTIFACT_QUERY)
                .get()
                .thenApply(wsResponse -> {
                    if (wsResponse.getStatus() == HttpResponseStatus.OK.code()) {
                        AndroidSDKVersion androidSDKVersion = Json.fromJson(wsResponse.asJson(), AndroidSDKVersion.class);
                        String latestVersion = androidSDKVersion.getLatestVersion();
                        return latestVersion == null ? DEFAULT_VERSION_NAME : latestVersion;
                    } else {
                        return DEFAULT_VERSION_NAME;
                    }
                });
    }

    private String getCacheKey(Platform platform) {
        switch (platform) {
            case IOS:
                return IOS_SDK_VERSION_CACHE_KEY;
            case ANDROID:
            default:
                return ANDROID_SDK_VERSION_CACHE_KEY;
        }
    }
}
