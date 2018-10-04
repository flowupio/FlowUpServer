package models;

import be.objectify.deadbolt.java.models.Permission;
import com.avaje.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.UUID;

@Entity
public class UserPermission extends Model implements Permission, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private UUID id;

    private String value;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
