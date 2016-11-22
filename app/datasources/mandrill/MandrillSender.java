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
    private final String signing_up_disabled_template;
    private final String fromEmail;
    private final String fromName;
    private final String companyName;
    private final String signing_up_disabled_subject;
    private final String sign_up_approved_template;
    private final String sign_up_approved_subject;

    @Inject
    public MandrillSender(MandrillClient client, @Named("mandrill") Configuration configuration) {
        this.client = client;
        this.fromEmail = configuration.getString("from_email");
        this.fromName = configuration.getString("from_name");
        this.companyName = configuration.getString("company");

        this.signing_up_disabled_template = configuration.getString("signing_up_disabled.template");
        this.signing_up_disabled_subject = configuration.getString("signing_up_disabled.subject");

        this.sign_up_approved_template = configuration.getString("sign_up_approved.template");
        this.sign_up_approved_subject = configuration.getString("sign_up_approved.subject");
    }

    @Override
    public CompletionStage<Boolean> sendSigningUpDisabledMessage(User user) {
        Recipient recipient = new Recipient(user.getEmail(), user.getName(), TO);
        Var[] globalMergeVars = new Var[] { new Var(COMPANY, companyName)};
        Message message = new Message(
                this.signing_up_disabled_subject,
                this.fromEmail,
                this.fromName,
                Collections.singletonList(recipient),
                globalMergeVars);
        return this.client.sendMessageWithTemplate(this.signing_up_disabled_template, message).thenApply(response -> response.getCode() == 200);
    }

    @Override
    public CompletionStage<Boolean> sendSignUpApprovedMessage(User user) {
        Recipient recipient = new Recipient(user.getEmail(), user.getName(), TO);
        Var[] globalMergeVars = new Var[] { new Var(COMPANY, companyName)};
        Message message = new Message(
                this.sign_up_approved_subject,
                this.fromEmail,
                this.fromName,
                Collections.singletonList(recipient),
                globalMergeVars);
        return this.client.sendMessageWithTemplate(this.sign_up_approved_template, message).thenApply(response -> response.getCode() == 200);
    }
}
