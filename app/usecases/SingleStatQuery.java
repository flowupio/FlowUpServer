package usecases;

import lombok.Data;
import models.Application;
import play.libs.F;

import java.time.Instant;

@Data
public class SingleStatQuery {
    private final Application application;
    private final String field;
    private final F.Tuple<Instant, Instant> bounds;
    private String queryStringValue = "*";

    public Instant getFrom() {
        return this.bounds._1;
    }

    public Instant getTo() {
        return this.bounds._2;
    }
}
