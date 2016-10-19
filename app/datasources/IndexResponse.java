package datasources;

import lombok.Data;

@Data
public class IndexResponse extends ActionWriteResponse {
    private final String id;
    private final String type;
    private final long version;
    private final boolean created;

    public IndexResponse(String index, String type, String id, long version, boolean created) {
        super(index);
        this.type = type;
        this.id = id;
        this.version = version;
        this.created = created;
    }
}
