import com.feth.play.module.pa.Resolver;
import com.feth.play.module.pa.providers.oauth2.github.GithubAuthProvider;
import com.feth.play.module.pa.providers.oauth2.google.GoogleAuthProvider;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import datasources.ElasticSearchDatasource;
import play.Configuration;
import play.Environment;
import service.AuthenticationResolver;
import service.FlowUpUserService;
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


        // play-authenticate dependencies
        bind(Resolver.class).to(AuthenticationResolver.class);
        // Following class depend on PlayAuthenticate auth, and they self register to it.
        bind(GithubAuthProvider.class).asEagerSingleton();
        bind(GoogleAuthProvider.class).asEagerSingleton();
        bind(FlowUpUserService.class).asEagerSingleton();
    }
}
