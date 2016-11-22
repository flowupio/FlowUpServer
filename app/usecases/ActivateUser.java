package usecases;

import com.google.inject.Inject;
import usecases.repositories.UserRepository;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

public class ActivateUser {
    private final UserRepository userRepository;

    @Inject
    public ActivateUser(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public CompletionStage<Boolean> execute(UUID userId) {
        return userRepository.activateByUserId(userId);
    }
}
