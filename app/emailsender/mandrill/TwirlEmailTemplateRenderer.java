package emailsender.mandrill;

import models.Application;
import emailsender.EmailTemplateRenderer;
import usecases.models.KeyStatCard;
import views.html.api.findbugs;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class TwirlEmailTemplateRenderer implements EmailTemplateRenderer {

    private static final String HTTP_APP_FLOWUP_IO = "http://app.flowup.io";

    @Override
    public CompletionStage<String> findbugs(Application application, List<KeyStatCard> statCards) {
        return CompletableFuture.supplyAsync(() -> findbugs.render(application, statCards, HTTP_APP_FLOWUP_IO).body());
    }
}
