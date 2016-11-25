package usecases.models;

import java.util.Collections;
import java.util.List;

public class KeyStatCard {
    private final StatCard main;
    private final String description;
    private List<StatCard> details;

    public KeyStatCard(StatCard main, String description) {
        this.main = main;
        this.description = description;
        this.details = Collections.emptyList();
    }

    public StatCard getMain() {
        return main;
    }

    public List<StatCard> getDetails() {
        return details;
    }

    public void setDetails(List<StatCard> details) {
        this.details = details;
    }

    public String getDescription() {
        return description;
    }
}
