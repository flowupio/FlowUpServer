package module;

import com.feth.play.module.pa.Resolver;
import com.feth.play.module.pa.providers.oauth2.google.GoogleAuthProvider;
import com.google.inject.AbstractModule;
import service.AuthenticationResolver;
import service.FlowUpUserService;

public class AuthenticationModule extends AbstractModule {

    protected void configure() {
        // play-authenticate dependencies
        bind(Resolver.class).to(AuthenticationResolver.class);
        // Following class depend on PlayAuthenticate auth, and they self register to it.
        bind(GoogleAuthProvider.class).asEagerSingleton();
        bind(FlowUpUserService.class).asEagerSingleton();
    }
}
