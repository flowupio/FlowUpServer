package usecases.models;

public final class KeyStatRow {
    private final StatCard first;
    private final StatCard second;

    public KeyStatRow(StatCard first, StatCard second) {
        this.first = first;
        this.second = second;
    }

    public StatCard getFirst() {
        return first;
    }

    public StatCard getSecond() {
        return second;
    }
}
