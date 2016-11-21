package usecases;

import models.User;

import java.util.concurrent.CompletionStage;

public interface EmailDatasource {
    CompletionStage<Boolean> sendSigningUpDisabledMessage(User user);
}
