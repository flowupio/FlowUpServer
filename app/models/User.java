package models;

import com.avaje.ebean.Model;
import play.data.validation.Constraints;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User extends Model {
    private static final long serialVersionUID = 1L;

    @Id
    public UUID id;

    @Constraints.Email
    @Column(unique = true)
    public String email;

    public String name;

    public boolean active;

    public boolean emailValidated;
}
