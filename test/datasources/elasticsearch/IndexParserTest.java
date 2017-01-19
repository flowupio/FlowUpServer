package datasources.elasticsearch;

import org.junit.Test;
import utils.WithResources;

import java.util.List;

import static org.junit.Assert.*;

public class IndexParserTest implements WithResources {

    private static final String ANY_INDEX_VALUE = "yellow open index1 5 1 12 0 38.7kb 38.7kb";

    private final IndexParser parser = new IndexParser();

    @Test
    public void parsesJustOneIndex() {
        Index index = parser.toIndex(ANY_INDEX_VALUE);

        assertEquals("index1", index.getName());
        assertEquals(12, index.getNumberOfDocuments());
    }

    @Test
    public void parsesABunchOfIndexes() throws Exception {
        String indexesListResponse = getFile("elasticsearch/es_get_indexes_response");

        List<Index> indexes = parser.toIndexes(indexesListResponse);

        assertEquals(2, indexes.size());
        assertEquals("index1", indexes.get(0).getName());
        assertEquals("index2", indexes.get(1).getName());
    }

    @Test
    public void returnsAnEmptyListIfTheIndexResponseIsEmpty() throws Exception {
        String indexesListResponse = getFile("elasticsearch/es_get_indexes_empty_response");

        List<Index> indexes = parser.toIndexes(indexesListResponse);

        assertTrue(indexes.isEmpty());
    }

}