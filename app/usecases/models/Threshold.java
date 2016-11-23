package usecases.models;

public enum Threshold {
    NO_DATA(10), SEVERE(25), WARNING(15), OK(0);

    private final double value;

    Threshold(double value) {
        this.value = value;
    }

    public boolean isWarningOrWorse() {
        return value >= WARNING.value;
    }
}
