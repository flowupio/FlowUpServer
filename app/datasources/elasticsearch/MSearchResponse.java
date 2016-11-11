package datasources.elasticsearch;

import lombok.Data;

import java.util.List;

@Data
public class MSearchResponse {
    private List<SearchResponse> responses;
}
