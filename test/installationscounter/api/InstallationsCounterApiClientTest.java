package installationscounter.api;

import apiclient.ApiClientTest;
import installationscounter.domain.Installation;
import models.Platform;
import models.Version;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

public class InstallationsCounterApiClientTest extends ApiClientTest {

    private static final String ANY_API_KEY = "2732d62102914ef99875d4c3e0f0b083";
    private static final long ANY_TIMESTAMP = 1482248233017L;
    private static final Installation ANY_INSTALLATION = new Installation("2732d62102914ef99875d4c3e0f0b083",
            "9c2a2994-b2b9-4297-81eb-231984ad056P",
            new Version(7, 49, 1, Platform.IOS, false)
            , ANY_TIMESTAMP);

    private InstallationsCounterApiClient installationsCounterApiClient;

    @Before
    public void startPlay() {
        super.startPlay();
        when(time.daysAgo(anyInt())).thenReturn(new DateTime(ANY_TIMESTAMP));
        installationsCounterApiClient = new InstallationsCounterApiClient(elasticsearchClient, time);
    }

    @Test
    public void sendsGetInstallationsCounterRequestToTheCorrectPathUsingAPostMethod() throws Exception {
        stubFor(post(urlEqualTo("/installations/counter/_search"))
                .willReturn(aResponse()
                        .withBody(getFile("installationscounter/getInstallationsCounterResponse.json"))
                        .withStatus(200)));

        installationsCounterApiClient.getInstallationCounter(ANY_API_KEY).toCompletableFuture().get();
    }

    @Test
    public void returnsZeroIfTheInstallationsCounterIndexDoesNotExist() throws Exception {
        stubFor(post(urlEqualTo("/installations/counter/_search"))
                .willReturn(aResponse()
                        .withBody(getFile("installationscounter/getInstallationsCounterResponseWithoutIndex.json"))
                        .withStatus(200)));

        long counter = installationsCounterApiClient.getInstallationCounter(ANY_API_KEY).toCompletableFuture().get();

        assertEquals(0L, counter);
    }

    @Test
    public void returnsTheNumberOfBucketsObtainedFromTheGetInstallationsCounterAggregation() throws Exception {
        stubFor(post(urlEqualTo("/installations/counter/_search"))
                .willReturn(aResponse()
                        .withBody(getFile("installationscounter/getInstallationsCounterResponse.json"))
                        .withStatus(200)));

        long counter = installationsCounterApiClient.getInstallationCounter(ANY_API_KEY).toCompletableFuture().get();

        assertEquals(2L, counter);
    }

    @Test
    public void sendsTheAggregationRequestToTheCorrectPath() throws Exception {
        stubFor(post(urlEqualTo("/installations/counter/_search"))
                .withRequestBody(equalTo(getFile("installationscounter/getInstallationsCounterRequest.json")))
                .willReturn(aResponse()
                        .withBody(getFile("installationscounter/getInstallationsCounterResponse.json"))
                        .withStatus(200)));

        installationsCounterApiClient.getInstallationCounter(ANY_API_KEY).toCompletableFuture().get();
    }

    @Test
    public void sendsIncrementInstallationsCounterToTheCorrectPathUsingAPostMethod() throws Exception {
        stubFor(post(urlEqualTo("/installations/counter"))
                .willReturn(aResponse()
                        .withBody(getFile("installationscounter/incrementInstallationsCounterResponse.json"))
                        .withStatus(200)));

        installationsCounterApiClient.incrementCounter(ANY_INSTALLATION).toCompletableFuture().get();
    }

    @Test
    public void sendsIncrementInstallationsCounterRequestBodyProperly() throws Exception {
        stubFor(post(urlEqualTo("/installations/counter"))
                .withRequestBody(equalTo(getFile("installationscounter/incrementInstallationsCounterRequest.json")))
                .willReturn(aResponse()
                        .withBody(getFile("installationscounter/incrementInstallationsCounterResponse.json"))
                        .withStatus(200)));

        installationsCounterApiClient.incrementCounter(ANY_INSTALLATION).toCompletableFuture().get();
    }

    @Test
    public void returnsTheInstallationCreatedAfterSendingTheRequets() throws Exception {
        stubFor(post(urlEqualTo("/installations/counter"))
                .willReturn(aResponse()
                        .withBody(getFile("installationscounter/incrementInstallationsCounterResponse.json"))
                        .withStatus(200)));

        Installation installation = installationsCounterApiClient.incrementCounter(ANY_INSTALLATION).toCompletableFuture().get();

        assertEquals(ANY_INSTALLATION, installation);
    }
}