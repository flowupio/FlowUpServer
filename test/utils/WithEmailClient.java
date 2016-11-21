package utils;

import datasources.mandrill.MandrillClient;
import datasources.mandrill.MessagesSendTemplateResponse;
import models.Application;
import models.User;
import usecases.DashboardsClient;

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
