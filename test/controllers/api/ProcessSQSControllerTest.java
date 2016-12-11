package controllers.api;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.Application;
import play.mvc.Http;
import play.mvc.Result;
import utils.WithFlowUpApplication;
import utils.WithResources;

import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.route;
import static play.inject.Bindings.bind;

@RunWith(MockitoJUnitRunner.class)
public class ProcessSQSControllerTest extends WithFlowUpApplication implements WithResources {

    @Mock
    private AmazonSQS amazonSQS;

    @Override
    protected Application provideApplication() {
        ReceiveMessageResult messageResult = mock(ReceiveMessageResult.class);
        Message message = new Message()
                .withBody(givenJsonSerializedIndexRequests())
                .withReceiptHandle(UUID.randomUUID().toString());
        when(messageResult.getMessages()).thenReturn(Collections.singletonList(message));
        when(amazonSQS.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(messageResult);


        return getGuiceApplicationBuilder()
                .overrides(bind(AmazonSQS.class).toInstance(amazonSQS))
                .build();
    }



    @Test
    public void returnsTheConfigAsEnabledIfTheApiKeyIsEnabled() {
        Http.RequestBuilder requestBuilder = fakeRequest("POST", "/bufferedReports");

        Result result = route(requestBuilder);

        assertEquals(OK, result.status());
    }
}