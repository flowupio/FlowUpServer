package usecases.models;

import java.util.Collections;
import java.util.List;

public class KeyStatCard {
    private final StatCard main;
    private List<StatCard> detail;

    public KeyStatCard(StatCard main) {
        this.main = main;
        this.detail = Collections.emptyList();
    }

    public StatCard getMain() {
        return main;
    }

    public List<StatCard> getDetail() {
        return detail;
    }

    public void setDetail(List<StatCard> detail) {
        this.detail = detail;
    }
}
