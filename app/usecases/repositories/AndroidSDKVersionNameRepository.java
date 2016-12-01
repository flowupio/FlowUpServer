package usecases.repositories;

import io.netty.handler.codec.http.HttpResponseStatus;
import play.cache.CacheApi;
import play.libs.Json;
import play.libs.ws.WSClient;
import usecases.models.AndroidSDKVersion;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public class AndroidSDKVersionNameRepository {

    private static final int ANDROID_SDK_VERSION_TTL = (int) TimeUnit.DAYS.toSeconds(1);
    private static final String MAVEN_CENTRAL_SEARCH_PATH = "solrsearch/select";
    private static final String ARTIFACT_QUERY = "g:\"io.flowup\" AND a:\"android-sdk\"";
    private static final String ARTIFACT_QUERY_KEY = "q";
    private static final String DEFAULT_VERSION_NAME = "<LATEST_VERSION>";
    private static final String MAVEN_CENTRAL_BASE_URL = "http://search.maven.org/";
    static final String ANDROID_SDK_VERSION_CACHE_KEY = "androidSdkVersionName";

    private final CacheApi cache;
    private final WSClient ws;
    private final String baseUrl;

    @Inject
    public AndroidSDKVersionNameRepository(CacheApi cache, WSClient ws) {
        this(cache, ws, MAVEN_CENTRAL_BASE_URL);
    }

    public AndroidSDKVersionNameRepository(CacheApi cache, WSClient ws, String baseUrl) {
        this.cache = cache;
        this.ws = ws;
        this.baseUrl = baseUrl + MAVEN_CENTRAL_SEARCH_PATH;
    }

    public CompletionStage<String> getLatestAndroidSDKVersionName() {
        String cachedLatestAndroidSDKVersionFromMavenCentral = cache.get(getCacheKey());
        if (cachedLatestAndroidSDKVersionFromMavenCentral != null) {
            return CompletableFuture.completedFuture(cachedLatestAndroidSDKVersionFromMavenCentral);
        } else {
            CompletionStage<String> latestAndroidSDKVersionFromMavenCentral = getLatestAndroidSDKVersionFromMavenCentral();
            return latestAndroidSDKVersionFromMavenCentral.thenApply(s -> {
                cache.set(getCacheKey(), s, ANDROID_SDK_VERSION_TTL);
                return s;
            });
        }
    }

    private CompletionStage<String> getLatestAndroidSDKVersionFromMavenCentral() {
        return ws.url(baseUrl)
                .setQueryParameter(ARTIFACT_QUERY_KEY, ARTIFACT_QUERY)
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

    private String getCacheKey() {
        return ANDROID_SDK_VERSION_CACHE_KEY;
    }
}
