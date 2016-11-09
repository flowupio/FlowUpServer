package models;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.Index;
import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.util.UUID;

@Entity
public class ApiKey extends Model {

    @Id
    private UUID id;

    @Constraints.Required
    @Index
    private String value;

    @OneToOne(mappedBy = "apiKey")
    private Organization organization;

    private boolean enabled;

    public static Finder<UUID, ApiKey> find = new Finder<>(ApiKey.class);

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
