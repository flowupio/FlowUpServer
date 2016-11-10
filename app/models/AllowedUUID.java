package models;

import com.avaje.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Entity
public class AllowedUUID extends Model {

    @Id
    private UUID id;
}
