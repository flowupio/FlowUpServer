package controllers.api;

import com.google.inject.Inject;
import datasources.database.ApplicationDatasource;
import models.Application;
import play.mvc.Controller;
import play.mvc.Result;
import usecases.EmailSender;
import usecases.GetKeyMetrics;
import usecases.models.StatCard;

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
        applicationDatasource.findAll().thenApply(applications -> {

            for (Application application : applications) {
                getKeyMetrics.execute(application).thenApply(statCards -> {
                    boolean sendEmail = false;
                    for (StatCard statCard : statCards) {
                        if (statCard.getThreshold().isWarningOrWorse()) {
                            sendEmail = true;
                            break;
                        }
                    }

                    // Send Email
                });
            }

        });

    }
}
