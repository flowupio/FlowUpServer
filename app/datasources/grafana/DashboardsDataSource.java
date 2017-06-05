package datasources.grafana;

import models.Platform;
import usecases.models.Dashboard;

import java.util.Collections;
import java.util.List;

public class DashboardsDataSource {
    public List<Dashboard> getDashboards(Platform platform) {
        switch (platform) {
            case IOS:
                break;
            case ANDROID:
            default:
                break;
        }
        return Collections.emptyList();
    }
}
