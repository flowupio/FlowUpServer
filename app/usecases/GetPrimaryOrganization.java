package usecases;

import models.Organization;
import models.User;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class GetPrimaryOrganization {
    public CompletionStage<Organization> execute(User user) {
        return CompletableFuture.supplyAsync(() -> user.getOrganizations().get(0));
    }
}

