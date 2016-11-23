package usecases;

import models.Application;
import usecases.models.StatCard;

import java.util.List;
import java.util.concurrent.CompletionStage;

public interface EmailTemplateRenderer {
    CompletionStage<String> findbugs(Application application, List<StatCard> statCards);
}
