package datasources.mandrill;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import play.Configuration;
import play.Logger;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.mvc.Http;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class MandrillClient {

    private static final String MESSAGES_SEND_TEMPLATE_ENDPOINT = "/api/1.0/messages/send-template.json";

    private final WSClient ws;
    private final String baseUrl;
    private final String apiKey;

    @Inject
    public MandrillClient(WSClient ws, @Named("mandrill") Configuration configuration) {
        this.ws = ws;

        String scheme = configuration.getString("scheme");
        String host = configuration.getString("host");
        this.baseUrl = scheme + "://" + host;

        this.apiKey = configuration.getString("api_key");
    }

    public CompletionStage<MessagesSendTemplateResponse> sendMessageWithTemplate(String templateName, Message message) {
        return this.sendMessageWithTemplate(templateName, Collections.emptyList(), message);
    }

    public CompletionStage<MessagesSendTemplateResponse> sendMessageWithTemplate(String templateName, List<TemplateContent> templateContents, Message message) {
        ObjectNode payload = Json.newObject();
        payload.put("key", this.apiKey)
                .put("template_name", templateName);
        payload.set("template_content", Json.toJson(templateContents));
        payload.set("message", Json.toJson(message));

        Logger.debug(payload.toString());
        return this.ws.url(baseUrl + MESSAGES_SEND_TEMPLATE_ENDPOINT).post(payload).thenApply(response -> {
                    Logger.debug(response.getBody());
                    if (response.getStatus() != Http.Status.OK) {
                        return Json.fromJson(response.asJson(), MessagesSendTemplateResponse.class);
                    } else {
                        return new MessagesSendTemplateResponse("success", 200, "", "");
                    }
                }
        );
    }
}
