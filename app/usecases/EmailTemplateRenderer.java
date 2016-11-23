package usecases;

import usecases.models.StatCard;

import java.util.List;

public interface EmailTemplateRenderer {
    String findbugs(models.Application application, List<StatCard> statCards);
}
