package datasources.elasticsearch;

import lombok.Data;

import java.util.List;

@Data
public class SearchGroup {

    private final List<SearchBucket> buckets;
}
