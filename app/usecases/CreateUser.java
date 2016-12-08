package usecases;

import com.feth.play.module.pa.user.AuthUser;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import models.Organization;
import models.User;
import play.Configuration;
import usecases.repositories.UserRepository;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class CreateUser {

    private final UserRepository userRepository;
    private final EmailSender emailSender;
    private final DashboardsClient dashboardsClient;
    private final Boolean userActiveByDefault;

    @Inject
    public CreateUser(UserRepository userRepository, EmailSender emailSender, DashboardsClient dashboardsClient, @Named("flowup") Configuration flowupConf) {
        this.userRepository = userRepository;
        this.emailSender = emailSender;
        this.dashboardsClient = dashboardsClient;
        this.userActiveByDefault = flowupConf.getBoolean("user_active_at_creation", true);
    }

    public CompletionStage<User> execute(AuthUser authUser) {
        boolean isActive = userActiveByDefault || userRepository.existsOrganizationByEmail(authUser);
        User user = userRepository.create(authUser, isActive);
        CompletionStage<Boolean> sendEmailCompletionStage = sendSigningUpEmail(user);

        CompletionStage<User> dashboardsCompletionStage = userRepository.joinOrganization(user).thenCompose(this::setupDashboards);

        return CompletableFuture.allOf(
                sendEmailCompletionStage.toCompletableFuture(),
                dashboardsCompletionStage.toCompletableFuture()
        ).thenApply(aVoid -> user);
    }

    private CompletionStage<Boolean> sendSigningUpEmail(User user) {
        if (!user.isActive()) {
            return emailSender.sendSigningUpDisabledMessage(user);
        } else {
            return emailSender.sendSignUpApprovedMessage(user);
        }
    }

    private CompletionStage<User> setupDashboards(User user) {
        return dashboardsClient.createUser(user).thenCompose(userWithGrafana -> {
            return joinApplicationDashboards(userWithGrafana, userWithGrafana.getOrganizations().get(0));
        });
    }

    private CompletionStage<User> joinApplicationDashboards(User user, Organization organization) {
        if (organization.getApplications().isEmpty()) {
            return CompletableFuture.completedFuture(user);
        }
        return addUserToApplications(user, organization).thenCompose(aVoid -> {
            return dashboardsClient.switchUserContext(user, organization.getApplications().get(0)).thenCompose(application -> {
                return dashboardsClient.deleteUserInDefaultOrganisation(user);
            });
        });
    }

    private CompletableFuture<Void> addUserToApplications(User user, Organization organization) {
        CompletableFuture[] completionStages = organization.getApplications().stream().map(application -> {
            return dashboardsClient.addUserToOrganisation(user, application);
        }).toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(completionStages);
    }
}
