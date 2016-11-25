package usecases;

import lombok.Data;
import models.Application;

import java.time.Instant;

@Data
public class SingleStatQuery {
    private final Application application;
    private final String field;
    private final Instant from;
    private final Instant to;
    private String queryStringValue = "*";
}
