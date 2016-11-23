package datasources.mandrill;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import models.User;
import play.Configuration;
import usecases.EmailSender;

import java.util.Collections;
import java.util.concurrent.CompletionStage;

public class MandrillSender implements EmailSender {
    private static final String COMPANY = "COMPANY";
    private static final String TO = "to";
    private final MandrillClient client;
    private final String fromEmail;
    private final String fromName;
    private final String companyName;
    private final String signingUpDisabledTemplate;
    private final String signingUpDisabledSubject;
    private final String signUpApprovedTemplate;
    private final String signUpApprovedSubject;

    @Inject
    public MandrillSender(MandrillClient client, @Named("mandrill") Configuration configuration) {
        this.client = client;
        this.fromEmail = configuration.getString("from_email");
        this.fromName = configuration.getString("from_name");
        this.companyName = configuration.getString("company");

        this.signingUpDisabledTemplate = configuration.getString("signing_up_disabled.template");
        this.signingUpDisabledSubject = configuration.getString("signing_up_disabled.subject");

        this.signUpApprovedTemplate = configuration.getString("sign_up_approved.template");
        this.signUpApprovedSubject = configuration.getString("sign_up_approved.subject");
    }

    @Override
    public CompletionStage<Boolean> sendSigningUpDisabledMessage(User user) {
        Recipient recipient = new Recipient(user.getEmail(), user.getName(), TO);
        Var[] globalMergeVars = new Var[] { new Var(COMPANY, companyName)};
        Message message = new Message(
                this.signingUpDisabledSubject,
                this.fromEmail,
                this.fromName,
                Collections.singletonList(recipient),
                globalMergeVars);
        return this.client.sendMessageWithTemplate(this.signingUpDisabledTemplate, message).thenApply(response -> response.getCode() == 200);
    }

    @Override
    public CompletionStage<Boolean> sendSignUpApprovedMessage(User user) {
        Recipient recipient = new Recipient(user.getEmail(), user.getName(), TO);
        Var[] globalMergeVars = new Var[] { new Var(COMPANY, companyName)};
        Message message = new Message(
                this.signUpApprovedSubject,
                this.fromEmail,
                this.fromName,
                Collections.singletonList(recipient),
                globalMergeVars);
        return this.client.sendMessageWithTemplate(this.signUpApprovedTemplate, message).thenApply(response -> response.getCode() == 200);
    }
}
