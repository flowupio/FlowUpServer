package usecases.models;

import java.util.List;

public class KeyStatDetail {

    private final String description;
    private final List<KeyStatRow> rows;

    public KeyStatDetail(String description, List<KeyStatRow> rows) {
        this.description = description;
        this.rows = rows;
    }

    public String getDescription() {
        return description;
    }

    public List<KeyStatRow> getRows() {
        return rows;
    }
}
