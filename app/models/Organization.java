package models;

import com.avaje.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import java.util.List;
import java.util.UUID;

@Entity
public class Organization {
    @Id
    public UUID id;

    public String name;

    @OneToOne
    public ApiKey apiKey;

    @ManyToMany
    public List<User> members;

    public static Model.Finder<UUID, Organization> find = new Model.Finder<>(Organization.class);

    public static void create(String value) {
        ApiKey apiKey = new ApiKey();
        apiKey.value = value;
        apiKey.save();
    }
}
