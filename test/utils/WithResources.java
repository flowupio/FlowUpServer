package utils;

import com.fasterxml.jackson.databind.JsonNode;
import datasources.elasticsearch.*;
import models.Application;
import models.Organization;
import org.apache.commons.io.IOUtils;
import play.libs.Json;
import play.mvc.Result;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static play.test.Helpers.contentAsString;

public interface WithResources {

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

    default Application givenAnyApplication() {
        Application application = mock(Application.class);
        Organization organization = mock(Organization.class);
        when(application.getOrganization()).thenReturn(organization);
        when(organization.getId()).thenReturn(UUID.randomUUID());
        return application;
    }
}