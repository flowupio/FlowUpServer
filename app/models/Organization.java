package models;

import com.avaje.ebean.ExpressionList;
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

    public String grafanaId;

    public static Model.Finder<UUID, Organization> find = new Model.Finder<>(Organization.class);

    public static Organization findByGoogleAccount(String googleAccount) {
        return getGoogleAccountUserFind(googleAccount).findUnique();
    }

    private static ExpressionList<Organization> getGoogleAccountUserFind(String googleAccount) {
        return find.where().eq("google_account", googleAccount);
    }

}
