package models;

import com.avaje.ebean.Model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Entity
public class Organization extends Model implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private UUID id;

    private String name;

    @OneToOne
    private ApiKey apiKey;

    @ManyToMany(cascade = CascadeType.ALL)
    private List<User> members;

    private String googleAccount;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Application> applications;

    private String billingId;

    public static Model.Finder<UUID, Organization> find = new Model.Finder<>(Organization.class);

    public static List<Organization> findByName(String name) {
        return find.where().eq("name", name).findList();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ApiKey getApiKey() {
        return apiKey;
    }

    public void setApiKey(ApiKey apiKey) {
        this.apiKey = apiKey;
    }

    public List<User> getMembers() {
        return members;
    }

    public void setMembers(List<User> members) {
        this.members = members;
    }

    public String getGoogleAccount() {
        return googleAccount;
    }

    public void setGoogleAccount(String googleAccount) {
        this.googleAccount = googleAccount;
    }

    public List<Application> getApplications() {
        return applications;
    }

    public void setApplications(List<Application> applications) {
        this.applications = applications;
    }

    public String getBillingId() {
        return billingId;
    }

    public void setBillingId(String billingId) {
        this.billingId = billingId;
    }
}
