package module;

import be.objectify.deadbolt.java.cache.HandlerCache;
import com.google.inject.AbstractModule;
import security.FlowUpHandlerCache;

public class SecurityModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(HandlerCache.class).to(FlowUpHandlerCache.class).asEagerSingleton();
    }
}
