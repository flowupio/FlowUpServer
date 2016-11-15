package models;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.Index;
import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.UniqueConstraint;
import java.util.UUID;

@Entity
@UniqueConstraint(columnNames = {"app_package", "organization_id"})
public class Application extends Model {

    @Id
    private UUID id;

    @Constraints.Required
    @Index
    private String appPackage;

    @Constraints.Required
    @ManyToOne
    private Organization organization;

    private String grafanaOrgId;

    public static Finder<UUID, Application> find = new Finder<>(Application.class);

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getAppPackage() {
        return appPackage;
    }

    public void setAppPackage(String appPackage) {
        this.appPackage = appPackage;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public String getGrafanaOrgId() {
        return grafanaOrgId;
    }

    public void setGrafanaOrgId(String grafanaOrgId) {
        this.grafanaOrgId = grafanaOrgId;
    }
}
