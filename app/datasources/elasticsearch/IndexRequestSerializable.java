package datasources.elasticsearch;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;

import java.io.Serializable;

public class IndexRequestSerializable implements Serializable {

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
        indexRequest.setSource(indexRequestSerializable.getSource());
        return indexRequest;
    }
}
