package usecases;

public interface Value {

    static BasicValue toBasicValue(double doubleValue) {
        return new BasicValue(doubleValue);
    }

}