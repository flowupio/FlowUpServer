package models;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.Index;
import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

@Entity
public class ApiKey extends Model {

    @Id
    private String id;

    @Constraints.Required
    @Index
    private String value;

    @OneToOne(mappedBy = "apiKey")
    private Organization organization;

    private boolean enabled;

    private int numberOfAllowedUUIDs;

    @OneToOne(mappedBy = "apiKey")
    private List<AllowedUUID> allowedUUIDs;

    public static Finder<UUID, ApiKey> find = new Finder<>(ApiKey.class);

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public int getNumberOfAllowedUUIDs() {
        return numberOfAllowedUUIDs;
    }

    public void setNumberOfAllowedUUIDs(int numberOfAllowedUUIDs) {
        this.numberOfAllowedUUIDs = numberOfAllowedUUIDs;
    }

    public List<AllowedUUID> getAllowedUUIDs() {
        if (allowedUUIDs == null) {
            allowedUUIDs = Collections.emptyList();
        }
        return allowedUUIDs;
    }

    public void setAllowedUUIDs(List<AllowedUUID> allowedUUIDs) {
        this.allowedUUIDs = allowedUUIDs;
    }

    public boolean containsAllowedUUID(String uuid) {
        return allowedUUIDs.stream()
                .filter(allowedUUID -> allowedUUID.getId().equals(uuid))
                .count() > 0;
    }
}
