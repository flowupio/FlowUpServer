package utils;

import akka.actor.ActorSystem;
import org.junit.Before;
import play.Logger;
import play.db.Database;
import play.test.WithApplication;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import scala.concurrent.Await;
import scala.concurrent.duration.Duration;
import scredis.Client;
import scredis.Redis;
import scredis.RedisConfig;
import scredis.RedisConfigDefaults;

public class WithFlowUpApplication extends WithApplication {

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
            Await.ready(client.flushDB(), Duration.create(1, TimeUnit.SECONDS));
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