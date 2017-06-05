package usecases.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import play.Logger;
import play.libs.Json;
import usecases.models.Dashboard;

import java.io.IOException;

public class DashboardMapper {

    private final ObjectMapper mapper;

    public DashboardMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public JsonNode map(Dashboard dashboard) {
        try {
            return Json.newObject()
                    .put("overwrite", true)
                    .set("dashboard", mapper.readTree(dashboard.getJsonDescription()));
        } catch (IOException e) {
            Logger.error("Failed mapping a dashboard to store it in Grafana", e);
            return Json.newObject()
                    .put("overwrite", false)
                    .put("dashboard", Json.newObject());
        }
    }
}
