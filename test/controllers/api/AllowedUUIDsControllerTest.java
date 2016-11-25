package controllers.api;

import models.ApiKey;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;
import play.mvc.Result;
import usecases.repositories.ApiKeyRepository;
import utils.WithFlowUpApplication;
import utils.WithResources;

import java.util.Collections;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static play.inject.Bindings.bind;
import static play.mvc.Http.Status.*;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.route;

@RunWith(MockitoJUnitRunner.class)
public class AllowedUUIDsControllerTest extends WithFlowUpApplication implements WithResources {

    private static final String API_KEY_VALUE = "35e25a2d1eaa464bab565f7f5e4bb029";

    @Mock
    private ApiKeyRepository apiKeyRepository;

    @Override
    protected Application provideApplication() {
        IsMessageSignatureValid isMessageSignatureValid = mock(IsMessageSignatureValid.class);
        when(isMessageSignatureValid.execute(any())).thenReturn(true);
        return new GuiceApplicationBuilder()
                .overrides(bind(ApiKeyRepository.class).toInstance(apiKeyRepository))
                .overrides(bind(IsMessageSignatureValid.class).toInstance(isMessageSignatureValid))
                .build();
    }

    @Test
    public void returnsOkIfThereAreApiKeys() {
        givenAnApiKey(API_KEY_VALUE, true);

        Result result = deleteYesterdayAllowedUUIDs();

        assertEquals(OK, result.status());
    }

    @Test
    public void returnsOkIfThereAreNoApiKeys() {
        Result result = deleteYesterdayAllowedUUIDs();

        assertEquals(OK, result.status());
    }

    @Test
    public void returnsOkWhenSubscribingToSNS() {
        Result result = subscriptionConfirmationToSNS();

        assertEquals(OK, result.status());
    }

    private Result deleteYesterdayAllowedUUIDs() {
        HashMap<String, String[]> hm = new HashMap<>();
        hm.put("x-amz-sns-message-type", new String[]{"Notification"});
        hm.put("x-amz-sns-message-id", new String[]{"da41e39f-ea4d-435a-b922-c6aae3915ebe"});
        hm.put("x-amz-sns-topic-arn", new String[]{"arn:aws:sns:us-west-2:123456789012:MyTopic"});
        hm.put("x-amz-sns-subscription-arn", new String[]{"arn:aws:sns:us-west-2:123456789012:MyTopic:2bcfbf39-05c3-41de-beaa-fcfcc21c8f55"});

        Http.RequestBuilder requestBuilder = fakeRequest("POST", "/allowedUUIDs")
                .headers(hm).header("Content-Type", "application/json").bodyText(getFile("sns/notification.json"));
        return route(requestBuilder);
    }

    private Result subscriptionConfirmationToSNS() {
        HashMap<String, String[]> hm = new HashMap<>();
        hm.put("x-amz-sns-message-type", new String[]{"SubscriptionConfirmation"});
        hm.put("x-amz-sns-message-id", new String[]{"165545c9-2a5c-472c-8df2-7ff2be2b3b1b"});
        hm.put("x-amz-sns-topic-arn", new String[]{"arn:aws:sns:us-west-2:123456789012:MyTopic"});
        hm.put("x-amz-sns-subscription-arn", new String[]{"arn:aws:sns:us-west-2:123456789012:MyTopic:2bcfbf39-05c3-41de-beaa-fcfcc21c8f55"});

        Http.RequestBuilder requestBuilder = fakeRequest("POST", "/allowedUUIDs")
                .headers(hm).header("Content-Type", "application/json").bodyText(getFile("sns/subscriptionConfirmation.json"));
        return route(requestBuilder);
    }

    private void givenAnApiKey(String apiKeyValue, boolean enabled) {
        ApiKey apiKey = new ApiKey();
        apiKey.setValue(apiKeyValue);
        apiKey.setEnabled(enabled);
        when(apiKeyRepository.getApiKey(apiKeyValue)).thenReturn(apiKey);
    }

}
