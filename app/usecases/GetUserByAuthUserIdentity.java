package usecases;

import com.feth.play.module.pa.user.AuthUserIdentity;
import models.User;

public class GetUserByAuthUserIdentity {

    public User execute(AuthUserIdentity identity) {
        return User.findByAuthUserIdentity(identity);
    }
}
