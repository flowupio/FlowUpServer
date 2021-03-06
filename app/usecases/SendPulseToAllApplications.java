package usecases;

import com.google.inject.Inject;
import com.spotify.futures.CompletableFutures;
import emailsender.EmailSender;
import emailsender.EmailTemplateRenderer;
import models.Application;
import models.User;
import usecases.models.KeyStatCard;
import usecases.repositories.ApplicationRepository;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class SendPulseToAllApplications {
    private final ApplicationRepository applicationRepository;
    private final GetKeyMetrics getKeyMetrics;
    private final EmailSender emailSender;
    private final EmailTemplateRenderer emailTemplateRenderer;

    @Inject
    public SendPulseToAllApplications(ApplicationRepository applicationRepository, GetKeyMetrics getKeyMetrics, EmailSender emailSender, EmailTemplateRenderer emailTemplateRenderer) {
        this.applicationRepository = applicationRepository;
        this.getKeyMetrics = getKeyMetrics;
        this.emailSender = emailSender;
        this.emailTemplateRenderer = emailTemplateRenderer;
    }

    public CompletionStage<List<Boolean>> execute() {
        return applicationRepository.findAll().thenCompose(this::processApplications);
    }

    private CompletionStage<List<Boolean>> processApplications(List<Application> applications) {
        List<CompletableFuture<Boolean>> completableFutures = new ArrayList<>();
        for (Application application : applications) {
            CompletionStage<Boolean> completionStage = getKeyMetrics.execute(application).thenCompose(statCards ->
                    processStatsCards(application, statCards));
            completableFutures.add(completionStage.toCompletableFuture());
        }
        return CompletableFutures.allAsList(completableFutures);
    }

    private CompletionStage<Boolean> processStatsCards(Application application, List<KeyStatCard> keyStatCards) {
        if (!isStatsCardThresholdWarningOrWorse(keyStatCards)) {
            return CompletableFuture.completedFuture(false);
        }

        return emailTemplateRenderer.findbugs(application, keyStatCards).thenCompose(content -> {
            List<User> members = application.getOrganization().getMembers();
            return emailSender.sendKeyMetricsMessage(members, application.getAppPackage(), ZonedDateTime.now(), content);
        });
    }

    private boolean isStatsCardThresholdWarningOrWorse(List<KeyStatCard> keyStatCards) {
        for (KeyStatCard keyStatCard : keyStatCards) {
            if (keyStatCard.getMain().getThreshold().isWarningOrWorse()) {
                return true;
            }
        }
        return false;
    }
}

