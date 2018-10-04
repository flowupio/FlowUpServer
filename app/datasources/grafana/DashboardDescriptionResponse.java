package datasources.grafana;

import lombok.Data;

import java.util.List;

@Data
public class DashboardDescriptionResponse {
    private int id;
    private String title;
    private String uri;
    private String type;
    private List<String> tags;
    private boolean isStarred;
}
