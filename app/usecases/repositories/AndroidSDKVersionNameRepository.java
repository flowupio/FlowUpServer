package usecases.repositories;

import io.netty.handler.codec.http.HttpResponseStatus;
import play.cache.CacheApi;
import play.libs.Json;
import play.libs.ws.WSClient;
import usecases.models.AndroidSDKVersion;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public class AndroidSDKVersionNameRepository {

    private static final int ANDROID_SDK_VERSION_TTL = (int) TimeUnit.DAYS.toSeconds(1);
    private static final String MAVEN_CENTRAL_QUERY = "https://search.maven.org/solrsearch/select";
    private static final String ARTIFACT_QUERY = "g:\"io.flowup\" AND a:\"android-sdk\"";
    private static final String ARTIFACT_QUERY_KEY = "q";
    private static final String DEFAULT_VERSION_NAME = "<LATEST_VERSION>";

    private final CacheApi cache;
    private final WSClient ws;

    @Inject
    public AndroidSDKVersionNameRepository(CacheApi cache, WSClient ws) {
        this.cache = cache;
        this.ws = ws;
    }

    public CompletionStage<String> getLatestAndroidSDKVersionName() {
        return cache.getOrElse(getCacheKey(),
                this::getLatestAndroidSDKVersionFromMavenCentral,
                ANDROID_SDK_VERSION_TTL);
    }

    private CompletionStage<String> getLatestAndroidSDKVersionFromMavenCentral() {
        return ws.url(MAVEN_CENTRAL_QUERY)
                .setQueryParameter(ARTIFACT_QUERY_KEY, ARTIFACT_QUERY)
                .get()
                .thenApply(wsResponse -> {
            if (wsResponse.getStatus() == HttpResponseStatus.OK.code()) {
                AndroidSDKVersion androidSDKVersion = Json.fromJson(wsResponse.asJson(), AndroidSDKVersion.class);
                return androidSDKVersion.getLatestVersion();
            } else {
                return DEFAULT_VERSION_NAME;
            }
        });
    }

    private String getCacheKey() {
        return "androidSdkVersionName";
    }
}
