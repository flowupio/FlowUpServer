package usecases;

import lombok.Data;

@Data
public class BasicValue implements Value {
    private final double value;
}
