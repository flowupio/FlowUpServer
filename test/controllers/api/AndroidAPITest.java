package controllers.api;

import org.junit.Test;

import static org.junit.Assert.*;

public class AndroidAPITest {

    @Test
    public void extractsTheAPIValueFromAString() {
        AndroidAPI api = AndroidAPI.fromString("API25");

        assertEquals(new AndroidAPI(25), api);
    }

    @Test
    public void returnsTheMaxAPIPossibleIfTheInputStringIsNotValid() {
        AndroidAPI api = AndroidAPI.fromString("~25");

        assertEquals(new AndroidAPI(Integer.MAX_VALUE), api);
    }

    @Test
    public void compareLowerApiValuesShouldReturnNegativeValuesOnCompare() {
        AndroidAPI api19 = new AndroidAPI(19);
        AndroidAPI api25 = new AndroidAPI(25);

        assertTrue(api19.compareTo(api25) < 0);
    }

    @Test
    public void equalApiValuesShouldReturnZeroOnCompare() {
        AndroidAPI api25 = new AndroidAPI(25);

        assertEquals(0, api25.compareTo(api25));
    }

    @Test
    public void compareGreaterApiValuesShouldReturnPositiveValuesOnCompare() {
        AndroidAPI api19 = new AndroidAPI(19);
        AndroidAPI api25 = new AndroidAPI(25);

        assertTrue(api25.compareTo(api19) > 0);
    }


    @Test
    public void equalApiValuesShouldBeEqual() {
        AndroidAPI api = new AndroidAPI(25);
        AndroidAPI otherApi = new AndroidAPI(25);

        assertEquals(api, otherApi);
    }

}