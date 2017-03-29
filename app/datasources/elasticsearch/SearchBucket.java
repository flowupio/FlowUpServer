package datasources.elasticsearch;

import lombok.Data;

@Data
public class SearchBucket {

    private final String key;
    private final int docCount;

}
