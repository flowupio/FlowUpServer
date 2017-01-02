package models;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.Index;
import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.io.Serializable;
import java.util.UUID;

@Entity
public class BillingId extends Model implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private UUID id;

    @Constraints.Required
    @Index
    private String value;

    @OneToOne(mappedBy = "apiKey")
    private Organization organization;

    public static Finder<UUID, BillingId> find = new Finder<>(BillingId.class);

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
}
