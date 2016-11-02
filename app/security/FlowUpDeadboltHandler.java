package security;

import be.objectify.deadbolt.java.AbstractDeadboltHandler;
import be.objectify.deadbolt.java.ExecutionContextProvider;
import be.objectify.deadbolt.java.models.Subject;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUserIdentity;
import models.User;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class FlowUpDeadboltHandler extends AbstractDeadboltHandler {

    private final PlayAuthenticate auth;

    public FlowUpDeadboltHandler(final PlayAuthenticate auth, ExecutionContextProvider ecProvider) {
        super(ecProvider);
        this.auth = auth;
    }

    @Override
    public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context) {
        final AuthUserIdentity u = this.auth.getUser(context);
        return CompletableFuture.completedFuture(Optional.ofNullable((Subject) User.findByAuthUserIdentity(u)));
    }

    @Override
    public CompletionStage<Optional<Result>> beforeAuthCheck(final Http.Context context) {
        if (this.auth.isLoggedIn(context.session())) {
            return CompletableFuture.completedFuture(Optional.empty());
        } else {
            final String originalUrl = this.auth.storeOriginalUrl(context);

            context.flash().put("error",
                    "You need to log in first, to view '" + originalUrl + "'");
            return CompletableFuture.completedFuture(Optional.of(redirect(this.auth.getResolver().login())));
        }
    }
}
