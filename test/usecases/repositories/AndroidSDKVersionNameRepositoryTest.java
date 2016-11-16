package usecases.repositories;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import play.cache.CacheApi;
import play.libs.ws.WSClient;
import utils.WithFlowUpApplication;
import utils.WithResources;

import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.junit.Assert.assertEquals;

public class AndroidSDKVersionNameRepositoryTest extends WithFlowUpApplication implements WithResources {

    public static final String MAVEN_CENTRAL_SEARCH_QUERY = "/solrsearch/select?q=g%3A%22io.flowup%22%20AND%20a%3A%22android-sdk%22";
    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    private AndroidSDKVersionNameRepository repository;

    @Before
    public void startPlay() {
        super.startPlay();
        CacheApi cacheApi = app.injector().instanceOf(CacheApi.class);
        WSClient ws = app.injector().instanceOf(WSClient.class);
        repository = new AndroidSDKVersionNameRepository(cacheApi, ws, "http://localhost:8080/");
        cacheApi.remove(AndroidSDKVersionNameRepository.ANDROID_SDK_VERSION_CACHE_KEY);
    }

    @Test
    public void parsesAndReturnsTheAndroidSDKArtifactVersion() throws Exception {
        stubFor(get(urlEqualTo(MAVEN_CENTRAL_SEARCH_QUERY))
                .willReturn(aResponse()
                        .withBody(getFile("mavencentral/mavenCentralSearchArtifactSearchResponse.json"))
                        .withStatus(200)));

        String sdkVersion = repository.getLatestAndroidSDKVersionName().toCompletableFuture().get();

        assertEquals("0.1.4", sdkVersion);
    }

    @Test
    public void returnsTheDefaultVersionIfTheResponseIsAnError() throws Exception {
        stubFor(get(urlEqualTo(MAVEN_CENTRAL_SEARCH_QUERY))
                .willReturn(aResponse()
                        .withStatus(404)));

        String sdkVersion = repository.getLatestAndroidSDKVersionName().toCompletableFuture().get();

        assertEquals("<LATEST_VERSION>", sdkVersion);
    }

    @Test
    public void returnsTheDefaultVersionIfTheSearchResponseIsEmpty() throws Exception {
        stubFor(get(urlEqualTo(MAVEN_CENTRAL_SEARCH_QUERY))
                .willReturn(aResponse()
                        .withBody(getFile("mavencentral/mavenCentralSearchArtifactEmptyResponse.json"))
                        .withStatus(200)));

        String sdkVersion = repository.getLatestAndroidSDKVersionName().toCompletableFuture().get();

        assertEquals("<LATEST_VERSION>", sdkVersion);
    }

}