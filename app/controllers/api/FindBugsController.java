package controllers.api;

import com.google.inject.Inject;
import com.spotify.futures.CompletableFutures;
import datasources.database.ApplicationDatasource;
import models.Application;
import models.User;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;
import usecases.EmailSender;
import usecases.GetKeyMetrics;
import usecases.models.StatCard;
import views.html.api.findbugs;
import views.html.commandcenter.application;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class FindBugsController extends Controller {

    private final ApplicationDatasource applicationDatasource;
    private final GetKeyMetrics getKeyMetrics;
    private final EmailSender emailSender;

    @Inject
    public FindBugsController(ApplicationDatasource applicationDatasource, GetKeyMetrics getKeyMetrics, EmailSender emailSender) {
        this.applicationDatasource = applicationDatasource;
        this.getKeyMetrics = getKeyMetrics;
        this.emailSender = emailSender;
    }

    public CompletionStage<Result> index() {
        return applicationDatasource.findAll().thenCompose(applications -> {

            List<CompletableFuture<Boolean>> completableFutures = new ArrayList<>();
            for (Application application : applications) {
                CompletionStage<Boolean> completionStage = getKeyMetrics.execute(application).thenCompose(statCards -> {
                    boolean sendEmail = false;
                    for (StatCard statCard : statCards) {
                        if (statCard.getThreshold().isWarningOrWorse()) {
                            sendEmail = true;
                            break;
                        }
                    }

                    if (!sendEmail) {
                        return CompletableFuture.completedFuture(false);
                    }
                    Html content = findbugs.render(application, statCards);
                    List<User> members = application.getOrganization().getMembers();
                    return emailSender.sendKeyMetricsMessage(members, application.getAppPackage(), ZonedDateTime.now(),content.body());
                });
                completableFutures.add(completionStage.toCompletableFuture());
            }
            return CompletableFutures.allAsList(completableFutures);

        }).thenApply(booleen -> ok());
    }
}
