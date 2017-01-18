package datasources.elasticsearch;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

class IndexParser {

    Index toIndex(String value) {
        List<String> parts = Arrays.stream(value
                .trim().split(" "))
                .filter(s -> s.length() > 0)
                .collect(Collectors.toList());
        return new Index(parts.get(2), Integer.parseInt(parts.get(5)));
    }

    List<Index> toIndexes(String value) {
        List<Index> indexes = new LinkedList<>();
        String[] lines = value.split("\\n");
        for (int i = 1; i < lines.length; i++) {
            indexes.add(toIndex(lines[i]));
        }
        return indexes;
    }
}
