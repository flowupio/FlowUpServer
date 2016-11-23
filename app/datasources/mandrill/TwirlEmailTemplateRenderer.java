package datasources.mandrill;

import models.Application;
import usecases.EmailTemplateRenderer;
import usecases.models.StatCard;
import views.html.api.findbugs;

import java.util.List;

public class TwirlEmailTemplateRenderer implements EmailTemplateRenderer {
    @Override
    public String findbugs(Application application, List<StatCard> statCards) {
        return findbugs.render(application, statCards).body();
    }
}
