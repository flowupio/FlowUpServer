package datasources.grafana;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import models.Platform;
import play.Environment;
import play.Logger;
import usecases.models.Dashboard;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardsDataSource {

    private static final String ANDROID_DASHBOARDS_FILES_PATH = "resources/dashboards/android";
    private static final String IOS_DASHBOARDS_FILES_PATH = "resources/dashboards/ios";

    private final Environment environment;

    @Inject
    public DashboardsDataSource(Environment environment) {
        this.environment = environment;
    }

    public List<Dashboard> getDashboards(Platform platform) {
        switch (platform) {
            case IOS:
                return getDashboards(IOS_DASHBOARDS_FILES_PATH);
            case ANDROID:
            default:
                return getDashboards(ANDROID_DASHBOARDS_FILES_PATH);
        }
    }

    private List<Dashboard> getDashboards(String path) {
        return getDashboardsFileNames(path).stream()
                .map(this::readFile)
                .map(Dashboard::new)
                .collect(Collectors.toList());
    }

    private List<String> getDashboardsFileNames(String path) {
        File dashboardsDirectory = environment.getFile(path);
        File[] jsonFiles = dashboardsDirectory.listFiles((dir, name) -> name.endsWith("json"));
        return Arrays.stream(jsonFiles != null ? jsonFiles : new File[0])
                .map(File::getAbsolutePath)
                .collect(Collectors.toList());
    }

    private String readFile(String path) {
        try {
            return Files.toString(new File(path), Charsets.UTF_8);
        } catch (IOException e) {
            Logger.error("Unable to read dashboard file in " + path, e);
            return "";
        }
    }
}
