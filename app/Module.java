import be.objectify.deadbolt.java.cache.HandlerCache;
import com.feth.play.module.pa.Resolver;
import com.feth.play.module.pa.providers.oauth2.github.GithubAuthProvider;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import datasources.ElasticSearchDatasource;
import play.Configuration;
import play.Environment;
import security.FlowUpHandlerCache;
import service.FlowUpResolver;
import service.FlowUpUserService;
import service.UserService;
import usecases.MetricsDatasource;

public class Module extends AbstractModule {


    private final Environment environment;
    private final Configuration configuration;

    public Module(
            Environment environment,
            Configuration configuration) {
        this.environment = environment;
        this.configuration = configuration;
    }

    protected void configure() {
        Configuration elasticsearchConf = configuration.getConfig("elasticsearch");
        bind(Configuration.class)
                .annotatedWith(Names.named("elasticsearch"))
                .toInstance(elasticsearchConf);

        bind(MetricsDatasource.class)
                .to(ElasticSearchDatasource.class)
                .asEagerSingleton();


        bind(Resolver.class).to(FlowUpResolver.class);
        bind(HandlerCache.class).to(FlowUpHandlerCache.class);
        bind(GithubAuthProvider.class).asEagerSingleton();
        bind(UserService.class).asEagerSingleton();
        bind(FlowUpUserService.class).asEagerSingleton();
    }
}
