package utils;

import org.junit.After;
import play.Logger;
import play.db.Database;
import play.test.WithApplication;

import java.sql.SQLException;
import java.sql.Statement;

public class WithFlowUpApplication extends WithApplication {

    @After
    @Override
    public void stopPlay() {
        cleanDatabase();
        super.stopPlay();
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
            statement.addBatch("SET FOREIGN_KEY_CHECKS = 1;");
            statement.executeBatch();
        } catch (SQLException e) {
            Logger.error(e.getMessage());
        }
    }
}