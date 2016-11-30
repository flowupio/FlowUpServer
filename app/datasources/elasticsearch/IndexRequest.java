package datasources.elasticsearch;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import play.libs.Json;

import java.io.Serializable;

@Data
public class IndexRequest {

    private final IndexAction action;
    private JsonNode source;

    public IndexRequest(String index, String type) {
        this.action = new IndexAction(index, type);
    }

    public IndexRequest(IndexAction action) {
        this.action = action;
    }

    static IndexRequestSerializable toIndexRequestSerializable(IndexRequest indexRequest) {
        IndexRequestSerializable indexRequestSerializable = new IndexRequestSerializable();
        indexRequestSerializable.setAction(indexRequest.getAction());
        indexRequestSerializable.setSource(indexRequest.getSource());
        return indexRequestSerializable;
    }
}

class IndexRequestSerializable implements Serializable {

    private static final long serialVersionUID = 1L;

    public IndexAction getAction() {
        return action;
    }

    public JsonNode getSource() {
        return Json.parse(source);
    }

    public void setSource(JsonNode source) {
        this.source = source.toString();
    }

    public void setAction(IndexAction action) {
        this.action = action;
    }

    private IndexAction action;
    private String source;


    static IndexRequest toIndexRequest(IndexRequestSerializable indexRequestSerializable) {
        IndexRequest indexRequest = new IndexRequest(indexRequestSerializable.getAction());
        indexRequest.setSource(indexRequest.getSource());
        return indexRequest;
    }
}

