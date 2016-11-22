package usecases;

import models.User;

import java.util.concurrent.CompletionStage;

public interface EmailSender {
    CompletionStage<Boolean> sendSigningUpDisabledMessage(User user);
    CompletionStage<Boolean> sendSignUpApprovedMessage(User user);
}
