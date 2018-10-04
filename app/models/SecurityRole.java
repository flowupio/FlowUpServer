package models;

import be.objectify.deadbolt.java.models.Role;
import com.avaje.ebean.Model;
import play.data.validation.Constraints;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.UUID;

@Entity
public class SecurityRole extends Model implements Role, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private UUID id;

    @Constraints.Required
    @Column(unique = true)
    private String name;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
