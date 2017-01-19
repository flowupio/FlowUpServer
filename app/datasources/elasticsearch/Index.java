package datasources.elasticsearch;

import lombok.Data;

@Data
class Index {

    private final String name;
    private final int numberOfDocuments;

    public boolean isEmpty() {
        return numberOfDocuments == 0;
    }
}
