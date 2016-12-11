package utils;

import com.fasterxml.jackson.databind.JsonNode;
import models.Application;
import models.Organization;
import org.apache.commons.io.IOUtils;
import play.libs.Json;
import play.mvc.Result;

import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
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
        return givenAnyApplicationWithOrganizationId(UUID.randomUUID());
    }

    default Application givenAnyApplicationWithOrganizationId(UUID uuid) {
        Application application = mock(Application.class);
        Organization organization = mock(Organization.class);
        when(application.getOrganization()).thenReturn(organization);
        when(organization.getId()).thenReturn(uuid);
        return application;
    }

    default String givenJsonSerializedIndexRequests() {
        return  "[{\"action\":{\"index\":{\"_index\":\"flowup-9cdc0b15-bdb0-4209-a3d2-3bc7012d9793-io.flowup.app\",\"_type\":\"network_data\"}},\"source\":{\"@timestamp\":123456789,\"BytesUploaded\":1024.0,\"BytesDownloaded\":2048.0,\"AppPackage\":\"io.flowup.example\",\"DeviceModel\":\"Nexus 5X\",\"ScreenDensity\":\"xxhdpi\",\"ScreenSize\":\"800X600\",\"InstallationUUID\":\"123456789\",\"NumberOfCores\":\"4\",\"VersionName\":\"1.0.0\",\"AndroidOSVersion\":\"API24\",\"BatterySaverOn\":\"true\"}},{\"action\":{\"index\":{\"_index\":\"flowup-9cdc0b15-bdb0-4209-a3d2-3bc7012d9793-io.flowup.app\",\"_type\":\"ui_data\"}},\"source\":{\"@timestamp\":123456789,\"FrameTime\":{\"mean\":60.0,\"p10\":60.0,\"p90\":60.0},\"FramesPerSecond\":{\"mean\":1.6666666666666666E7,\"p10\":1.6666666666666666E7,\"p90\":1.6666666666666666E7},\"OnActivityCreatedTime\":{\"mean\":60.0,\"p10\":60.0,\"p90\":60.0},\"OnActivityStartedTime\":{\"mean\":60.0,\"p10\":60.0,\"p90\":60.0},\"OnActivityResumedTime\":{\"mean\":60.0,\"p10\":60.0,\"p90\":60.0},\"ActivityTime\":{\"mean\":60.0,\"p10\":60.0,\"p90\":60.0},\"OnActivityPausedTime\":{\"mean\":60.0,\"p10\":60.0,\"p90\":60.0},\"OnActivityStoppedTime\":{\"mean\":60.0,\"p10\":60.0,\"p90\":60.0},\"OnActivityDestroyedTime\":{\"mean\":60.0,\"p10\":60.0,\"p90\":60.0},\"AppPackage\":\"io.flowup.example\",\"DeviceModel\":\"Nexus 5X\",\"ScreenDensity\":\"xxhdpi\",\"ScreenSize\":\"800X600\",\"InstallationUUID\":\"123456789\",\"NumberOfCores\":\"4\",\"VersionName\":\"1.0.0\",\"AndroidOSVersion\":\"API24\",\"BatterySaverOn\":\"true\",\"ScreenName\":\"MainActivity\"}},{\"action\":{\"index\":{\"_index\":\"flowup-9cdc0b15-bdb0-4209-a3d2-3bc7012d9793-io.flowup.app\",\"_type\":\"cpu_data\"}},\"source\":{\"@timestamp\":123456789,\"Consumption\":10.0,\"AppPackage\":\"io.flowup.example\",\"DeviceModel\":\"Nexus 5X\",\"ScreenDensity\":\"xxhdpi\",\"ScreenSize\":\"800X600\",\"InstallationUUID\":\"123456789\",\"NumberOfCores\":\"4\",\"VersionName\":\"1.0.0\",\"AndroidOSVersion\":\"API24\",\"BatterySaverOn\":\"true\"}},{\"action\":{\"index\":{\"_index\":\"flowup-9cdc0b15-bdb0-4209-a3d2-3bc7012d9793-io.flowup.app\",\"_type\":\"memory_data\"}},\"source\":{\"@timestamp\":123456789,\"Consumption\":3.0,\"BytesAllocated\":1024.0,\"AppPackage\":\"io.flowup.example\",\"DeviceModel\":\"Nexus 5X\",\"ScreenDensity\":\"xxhdpi\",\"ScreenSize\":\"800X600\",\"InstallationUUID\":\"123456789\",\"NumberOfCores\":\"4\",\"VersionName\":\"1.0.0\",\"AndroidOSVersion\":\"API24\",\"BatterySaverOn\":\"true\"}},{\"action\":{\"index\":{\"_index\":\"flowup-9cdc0b15-bdb0-4209-a3d2-3bc7012d9793-io.flowup.app\",\"_type\":\"disk_data\"}},\"source\":{\"@timestamp\":123456789,\"InternalStorageWrittenBytes\":2048.0,\"SharedPreferencesWrittenBytes\":1024.0,\"AppPackage\":\"io.flowup.example\",\"DeviceModel\":\"Nexus 5X\",\"ScreenDensity\":\"xxhdpi\",\"ScreenSize\":\"800X600\",\"InstallationUUID\":\"123456789\",\"NumberOfCores\":\"4\",\"VersionName\":\"1.0.0\",\"AndroidOSVersion\":\"API24\",\"BatterySaverOn\":\"true\"}}]";
    }
}