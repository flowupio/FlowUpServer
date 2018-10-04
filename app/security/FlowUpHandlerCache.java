package security;

import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.ExecutionContextProvider;
import be.objectify.deadbolt.java.cache.HandlerCache;
import com.feth.play.module.pa.PlayAuthenticate;
import com.google.inject.Inject;

public class FlowUpHandlerCache implements HandlerCache {
    private DeadboltHandler defaultHandler;

    @Inject
    public FlowUpHandlerCache(final PlayAuthenticate auth, final ExecutionContextProvider execContextProvider) {
        this.defaultHandler = new FlowUpDeadboltHandler(auth, execContextProvider);
    }

    @Override
    public DeadboltHandler apply(String s) {
        return this.defaultHandler;
    }

    @Override
    public DeadboltHandler get() {
        return this.defaultHandler;
    }
}
