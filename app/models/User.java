package models;

import com.avaje.ebean.Model;
import play.data.validation.Constraints;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Entity
public class User extends Model {
    @Id
    public UUID id;

    @Constraints.Email
    @Column(unique = true)
    public String email;

    public String name;

    public boolean active;

    public boolean emailValidated;
}
