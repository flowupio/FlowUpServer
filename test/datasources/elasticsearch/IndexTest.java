package datasources.elasticsearch;

import org.junit.Test;

import static org.junit.Assert.*;

public class IndexTest {

    private static final String ANY_INDEX_NAME = "Index1";

    @Test
    public void anIndexIsEmptyIfTheNumberOfDocumentsIsZero() throws Exception {
        Index index = new Index(ANY_INDEX_NAME, 0);

        assertTrue(index.isEmpty());
    }

    @Test
    public void anIndexIsNotEmptyIfTheNumberOfDocumentsIsGreaterThanZero() throws Exception {
        Index index = new Index(ANY_INDEX_NAME, 1);

        assertFalse(index.isEmpty());
    }

}