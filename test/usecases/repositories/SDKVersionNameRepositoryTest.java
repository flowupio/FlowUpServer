package usecases.repositories;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import models.Platform;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import play.cache.CacheApi;
import play.libs.ws.WSClient;
import utils.WithFlowUpApplication;
import utils.WithResources;

import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.junit.Assert.assertEquals;

public class SDKVersionNameRepositoryTest extends WithFlowUpApplication implements WithResources {

    public static final String MAVEN_CENTRAL_SEARCH_QUERY = "/solrsearch/select?q=g%3A%22io.flowup%22%20AND%20a%3A%22android-sdk%22";
    public static final String COCOAPODS_SEARCH_QUERY = "/api/pods?query=FlowUpIOSSDK";
    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    private SDKVersionNameRepository repository;

    @Before
    public void startPlay() {
        super.startPlay();
        CacheApi cacheApi = app.injector().instanceOf(CacheApi.class);
        WSClient ws = app.injector().instanceOf(WSClient.class);
        repository = new SDKVersionNameRepository(cacheApi, ws, "http://localhost:8080/", "http://localhost:8080/");
        cacheApi.remove(SDKVersionNameRepository.ANDROID_SDK_VERSION_CACHE_KEY);
        cacheApi.remove(SDKVersionNameRepository.IOS_SDK_VERSION_CACHE_KEY);
    }

    @Test
    public void parsesAndReturnsTheAndroidSDKArtifactVersion() throws Exception {
        stubFor(get(urlEqualTo(MAVEN_CENTRAL_SEARCH_QUERY))
                .willReturn(aResponse()
                        .withBody(getFile("mavencentral/mavenCentralSearchArtifactSearchResponse.json"))
                        .withStatus(200)));

        String sdkVersion = repository.getLatestSDKVersionName(Platform.ANDROID).toCompletableFuture().get();

        assertEquals("0.1.4", sdkVersion);
    }

    @Test
    public void returnsTheDefaultVersionIfTheMavenCentralResponseIsAnError() throws Exception {
        stubFor(get(urlEqualTo(MAVEN_CENTRAL_SEARCH_QUERY))
                .willReturn(aResponse()
                        .withStatus(404)));

        String sdkVersion = repository.getLatestSDKVersionName(Platform.ANDROID).toCompletableFuture().get();

        assertEquals("<LATEST_VERSION>", sdkVersion);
    }

    @Test
    public void returnsTheDefaultVersionIfTheMavenCentralSearchResponseIsEmpty() throws Exception {
        stubFor(get(urlEqualTo(MAVEN_CENTRAL_SEARCH_QUERY))
                .willReturn(aResponse()
                        .withBody(getFile("mavencentral/mavenCentralSearchArtifactEmptyResponse.json"))
                        .withStatus(200)));

        String sdkVersion = repository.getLatestSDKVersionName(Platform.ANDROID).toCompletableFuture().get();

        assertEquals("<LATEST_VERSION>", sdkVersion);
    }

    @Test
    public void parsesAndReturnsTheIOSSDKArtifactVersion() throws Exception {
        stubFor(get(urlEqualTo(COCOAPODS_SEARCH_QUERY))
                .willReturn(aResponse()
                        .withBody(getFile("cocoapods/cocoapodsSearchArtifactResponse.json"))
                        .withStatus(200)));

        String sdkVersion = repository.getLatestSDKVersionName(Platform.IOS).toCompletableFuture().get();

        assertEquals("2.0.0", sdkVersion);
    }

    @Test
    public void returnsTheDefaultVersionIfTheCocoapodsResponseIsAnError() throws Exception {
        stubFor(get(urlEqualTo(COCOAPODS_SEARCH_QUERY))
                .willReturn(aResponse()
                        .withStatus(404)));

        String sdkVersion = repository.getLatestSDKVersionName(Platform.IOS).toCompletableFuture().get();

        assertEquals("<LATEST_VERSION>", sdkVersion);
    }

    @Test
    public void returnsTheDefaultVersionIfTheCocoapodsSearchResponseIsEmpty() throws Exception {
        stubFor(get(urlEqualTo(COCOAPODS_SEARCH_QUERY))
                .willReturn(aResponse()
                        .withBody(getFile("cocoapods/cocoapodsSearchArtifactEmptyResponse.json"))
                        .withStatus(200)));

        String sdkVersion = repository.getLatestSDKVersionName(Platform.IOS).toCompletableFuture().get();

        assertEquals("<LATEST_VERSION>", sdkVersion);
    }
}