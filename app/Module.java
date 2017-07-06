import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import datasources.elasticsearch.ElasticSearchDatasource;
import emailsender.mandrill.MandrillSender;
import emailsender.mandrill.TwirlEmailTemplateRenderer;
import usecases.DashboardsClient;
import datasources.grafana.GrafanaClient;
import play.Configuration;
import play.Environment;
import emailsender.EmailSender;
import emailsender.EmailTemplateRenderer;
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

        Configuration grafanaConf = configuration.getConfig("grafana");
        bind(Configuration.class)
                .annotatedWith(Names.named("grafana"))
                .toInstance(grafanaConf);

        Configuration airbrakeConf = configuration.getConfig("airbrake");
        bind(Configuration.class)
                .annotatedWith(Names.named("airbrake"))
                .toInstance(airbrakeConf);

        Configuration mandrillConf = configuration.getConfig("mandrill");
        bind(Configuration.class)
                .annotatedWith(Names.named("mandrill"))
                .toInstance(mandrillConf);


        Configuration flowupConf = configuration.getConfig("flowup");
        bind(Configuration.class)
                .annotatedWith(Names.named("flowup"))
                .toInstance(flowupConf);

        Configuration sqsConf = configuration.getConfig("sqs");
        bind(Configuration.class)
                .annotatedWith(Names.named("sqs"))
                .toInstance(sqsConf);

        Configuration taxamoConf = configuration.getConfig("taxamo");
        bind(Configuration.class)
                .annotatedWith(Names.named("taxamo"))
                .toInstance(taxamoConf);

        Configuration stripeConf = configuration.getConfig("stripe");
        bind(Configuration.class)
                .annotatedWith(Names.named("stripe"))
                .toInstance(stripeConf);

        bind(MetricsDatasource.class)
                .to(ElasticSearchDatasource.class)
                .asEagerSingleton();

        bind(DashboardsClient.class)
                .to(GrafanaClient.class)
                .asEagerSingleton();

        bind(EmailSender.class)
                .to(MandrillSender.class)
                .asEagerSingleton();


        bind(EmailTemplateRenderer.class)
                .to(TwirlEmailTemplateRenderer.class)
                .asEagerSingleton();

        bind(AmazonSQS.class)
                .to(AmazonSQSClient.class);
    }
}
