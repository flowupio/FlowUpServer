package utils;

import akka.actor.ActorSystem;
import datasources.elasticsearch.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.Logger;
import play.db.Database;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.WithApplication;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;
import scredis.Client;
import scredis.RedisConfigDefaults;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.when;
import static play.inject.Bindings.bind;

@RunWith(MockitoJUnitRunner.class)
public class WithFlowUpApplication extends WithApplication {

    @Mock
    protected ElasticsearchClient elasticsearchClient;

    protected GuiceApplicationBuilder getGuiceApplicationBuilder() {
        return new GuiceApplicationBuilder()
                .overrides(bind(ElasticsearchClient.class).toInstance(elasticsearchClient));
    }

    protected void setupSuccessfulElasticsearchClient() {
        ActionWriteResponse networkDataResponse = new IndexResponse("flowup-network_data", "counter", "AVe4CB89xL5tw_jvDTTd", 1, true);
        networkDataResponse.setShardInfo(new ActionWriteResponse.ShardInfo(2, 1));
        BulkItemResponse[] responses = {new BulkItemResponse(0, "index", networkDataResponse)};
        BulkResponse bulkResponse = new BulkResponse(responses, 67);

        when(elasticsearchClient.postBulk(anyListOf(IndexRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(bulkResponse));
    }

    @Before
    @Override
    public void startPlay() {
        super.startPlay();
        cleanDatabase();
        cleanCache();
    }

    private void cleanCache() {
        ActorSystem actorSystem = app.injector().instanceOf(ActorSystem.class);
        Client client = new Client(RedisConfigDefaults.Config(), actorSystem);
        try {
            Await.ready(client.flushAll(), Duration.create(1, TimeUnit.SECONDS));
        } catch (InterruptedException | TimeoutException e) {
            Logger.error(e.getMessage());
        }
    }

    private void cleanDatabase() {
        Database database = app.injector().instanceOf(Database.class);
        try {
            Statement statement = database.getConnection().createStatement();
            statement.addBatch("SET FOREIGN_KEY_CHECKS = 0;");
            statement.addBatch("TRUNCATE api_key;");
            statement.addBatch("TRUNCATE application;");
            statement.addBatch("TRUNCATE linked_account;");
            statement.addBatch("TRUNCATE organization;");
            statement.addBatch("TRUNCATE organization_user;");
            statement.addBatch("TRUNCATE security_role;");
            statement.addBatch("TRUNCATE user;");
            statement.addBatch("TRUNCATE user_permission;");
            statement.addBatch("TRUNCATE user_security_role;");
            statement.addBatch("TRUNCATE user_user_permission;");
            statement.addBatch("TRUNCATE allowed_uuid;");
            statement.addBatch("SET FOREIGN_KEY_CHECKS = 1;");
            statement.executeBatch();
        } catch (SQLException e) {
            Logger.error(e.getMessage());
        }
    }
}