import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import datasources.ElasticSearchDatasource;
import play.Configuration;
import play.Environment;
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
    }
}
