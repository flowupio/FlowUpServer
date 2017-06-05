package datasources.grafana;

import models.Platform;
import play.Environment;
import usecases.models.Dashboard;

import javax.inject.Inject;
import java.io.File;
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
                .map(Dashboard::new)
                .collect(Collectors.toList());
    }

    private List<String> getDashboardsFileNames(String path) {
        File dashboardsDirectory = environment.getFile(path);
        String[] dashboardFilesNames = dashboardsDirectory.list((dir, name) -> name.endsWith("json"));
        return Arrays.asList(dashboardFilesNames != null ? dashboardFilesNames : new String[0]);
    }

}
