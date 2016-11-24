package datasources.mandrill;

import models.Application;
import usecases.EmailTemplateRenderer;
import usecases.models.StatCard;
import views.html.api.findbugs;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class TwirlEmailTemplateRenderer implements EmailTemplateRenderer {

    private static final String HTTP_APP_FLOWUP_IO = "http://app.flowup.io";

    @Override
    public CompletionStage<String> findbugs(Application application, List<StatCard> statCards) {
        return CompletableFuture.supplyAsync(() -> findbugs.render(application, statCards, HTTP_APP_FLOWUP_IO).body());
    }
}
