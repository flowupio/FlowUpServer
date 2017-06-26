package usecases.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.Application;
import play.Logger;
import play.libs.Json;
import usecases.models.Dashboard;

import javax.inject.Inject;
import java.io.IOException;

public class DashboardMapper {

    private final ObjectMapper mapper;

    @Inject
    public DashboardMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public JsonNode map(Application application, Dashboard dashboard) {
        return Json.newObject()
                .put("overwrite", true)
                .put("orgId", application.getGrafanaOrgId())
                .set("dashboard", mapDashboard(dashboard));
    }

    private JsonNode mapDashboard(Dashboard dashboard) {
        try {
            return mapper.readTree(dashboard.getJsonDescription());
        } catch (IOException e) {
            Logger.error("Failed mapping a dashboard to store it in Grafana", e);
            return Json.newObject();
        }
    }
}
