package usecases;

import models.Organization;
import models.User;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class GetPrimaryOrganization {
    public CompletionStage<Organization> execute(User user) {
        return CompletableFuture.supplyAsync(() -> {
            List<Organization> organizations = user.getOrganizations();
            return organizations.get(0);
        });
    }
}

