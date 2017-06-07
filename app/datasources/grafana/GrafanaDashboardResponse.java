package datasources.grafana;

import lombok.Data;

@Data
public class GrafanaDashboardResponse {
    private String slug;
    private String status;
    private int version;
}
