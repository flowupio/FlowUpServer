package usecases;

import models.Application;
import models.Organization;
import models.User;

import javax.inject.Inject;
import java.util.UUID;

public class GetPrimaryOrganization {
    public Organization execute(User user) {
        return user.getOrganizations().get(0);
    }
}

