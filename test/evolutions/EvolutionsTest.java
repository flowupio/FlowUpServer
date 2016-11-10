package evolutions;

import org.junit.Test;
import play.db.Database;
import play.db.evolutions.Evolutions;
import utils.WithFlowUpApplication;

public class EvolutionsTest extends WithFlowUpApplication {

    @Test
    public void executesUpAndDownEvolutionsWithoutErrors() {
        Database database = app.injector().instanceOf(Database.class);
        Evolutions.cleanupEvolutions(database);
        Evolutions.applyEvolutions(database);
    }

}
