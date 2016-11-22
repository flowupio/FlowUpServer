package usecases;

import com.google.inject.Inject;
import models.User;
import usecases.repositories.UserRepository;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ActivateUser {
    private final UserRepository userRepository;
    private final EmailSender emailSender;

    @Inject
    public ActivateUser(UserRepository userRepository, EmailSender emailSender) {
        this.userRepository = userRepository;
        this.emailSender = emailSender;
    }

    public CompletionStage<Boolean> execute(UUID userId) {
        User user = userRepository.getById(userId);
        if (user == null) {
            return CompletableFuture.completedFuture(false);
        }

        return emailSender.sendSignUpApprovedMessage(user).thenApply(emailSent -> {
            if (emailSent) {
                userRepository.setActive(user);
            }
            return emailSent;
        });
    }
}
