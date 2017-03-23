package emailsender;

import models.Application;
import models.User;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.CompletionStage;

public interface EmailSender {
    CompletionStage<Boolean> sendSigningUpDisabledMessage(User user);
    CompletionStage<Boolean> sendSignUpApprovedMessage(User user);
    CompletionStage<Boolean> sendKeyMetricsMessage(List<User> users, String appPackage, ZonedDateTime dateTime, String topMetricsHtml);
    CompletionStage<Boolean> sendKeyMetricsMessage(List<User> users, Application app);
}
