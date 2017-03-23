package usecases;

import com.feth.play.module.pa.user.AuthUserIdentity;
import com.google.inject.Inject;
import models.User;
import usecases.repositories.UserRepository;

public class GetUserByAuthUserIdentity {

    private final UserRepository userRepository;

    @Inject
    public GetUserByAuthUserIdentity(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User execute(AuthUserIdentity identity) {
        return userRepository.getByAuthUserIdentity(identity);
    }
}
