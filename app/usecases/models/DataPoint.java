package usecases.models;


import lombok.Data;
import play.libs.F;

import java.util.Date;
import java.util.List;

@Data
public class DataPoint {
    private final Date timestamp;
    private final List<F.Tuple<String, Value>> measurements;
    private final List<F.Tuple<String, String>> tags;
}

