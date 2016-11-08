package utils;

import org.junit.After;
import play.db.Database;
import play.db.evolutions.Evolutions;
import play.test.WithApplication;

public class WithFlowUpApplication extends WithApplication {

    @After
    @Override
    public void stopPlay() {
        cleanDatabase();
        super.stopPlay();
    }

    private void cleanDatabase() {
        Database database = app.injector().instanceOf(Database.class);
        Evolutions.cleanupEvolutions(database);
    }
}