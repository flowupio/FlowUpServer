import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.io.IOUtils;
import play.libs.Json;
import play.mvc.Result;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static play.test.Helpers.contentAsString;

interface WithResources {

    default void assertEqualsString(String expect, Result result) {
        assertEquals(expect, contentAsString(result));
        assertEquals("application/json", result.contentType().get());
        assertEquals("UTF-8", result.charset().get());
    }

    default String getFile(String fileName){

        String result = "";

        ClassLoader classLoader = getClass().getClassLoader();
        try {
            result = IOUtils.toString(classLoader.getResourceAsStream(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;

    }

    default JsonNode contentAsJson(Result result) {
        return Json.parse(contentAsString(result));
    }
}