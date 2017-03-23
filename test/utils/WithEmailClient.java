package utils;

import emailsender.mandrill.MandrillClient;
import emailsender.mandrill.MessagesSendTemplateResponse;

import java.util.concurrent.CompletableFuture;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public interface WithEmailClient {
    default MandrillClient getMockEmailClient() {
        MandrillClient mandrillClient = mock(MandrillClient.class);
        when(mandrillClient.sendMessageWithTemplate(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(new MessagesSendTemplateResponse("success", 200, "", "")));
        return mandrillClient;
    }
}
