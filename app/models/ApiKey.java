package models;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.Index;
import play.data.validation.Constraints;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Entity
public class ApiKey extends Model {

    @Id
    public UUID id;

    @Constraints.Required
    @Index
    public String value;

    public static Finder<UUID, ApiKey> find = new Finder<>(ApiKey.class);

    public static void create(String value) {
        ApiKey apiKey = new ApiKey();
        apiKey.value = value;
        apiKey.save();
    }
}
