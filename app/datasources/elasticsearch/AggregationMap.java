package datasources.elasticsearch;

import java.util.HashMap;

public class AggregationMap extends HashMap<String, Aggregation> {
    public static AggregationMap singleton(String name, Aggregation aggsObject) {
        AggregationMap aggs = new AggregationMap();
        aggs.put(name, aggsObject);
        return aggs;
    }
}