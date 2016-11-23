package usecases;

import models.User;

import java.util.List;
import java.util.concurrent.CompletionStage;

public interface EmailSender {
    CompletionStage<Boolean> sendSigningUpDisabledMessage(User user);
    CompletionStage<Boolean> sendSignUpApprovedMessage(User user);
    CompletionStage<Boolean> sendKeyMetricsMessage(List<User> users);
}
