package datasources.mandrill;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import models.User;
import play.Configuration;
import usecases.EmailSender;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class MandrillSender implements EmailSender {
    private static final String COMPANY = "COMPANY";
    private static final String TO = "to";
    private static final String MAIN = "main";
    private final MandrillClient client;
    private final String fromEmail;
    private final String fromName;
    private final String companyName;
    private final String signingUpDisabledTemplate;
    private final String signingUpDisabledSubject;
    private final String signUpApprovedTemplate;
    private final String signUpApprovedSubject;
    private final String pulseTemplate;
    private final String pulseSubject;

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

        this.pulseTemplate = configuration.getString("pulse.template");
        this.pulseSubject = configuration.getString("pulse.subject");
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

    @Override
    public CompletionStage<Boolean> sendKeyMetricsMessage(List<User> users, String appPackage, ZonedDateTime dateTime, String topMetricsHtml) {
        List<Recipient> recipients = users.stream().map(user -> new Recipient(user.getEmail(), user.getName(), TO)).collect(Collectors.toList());
        Var[] globalMergeVars = new Var[] { new Var(COMPANY, companyName)};
        DateTimeFormatter format = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG);

        Message message = new Message(
                String.format(this.pulseSubject, appPackage, dateTime.format(format)),
                this.fromEmail,
                this.fromName,
                recipients,
                globalMergeVars);
        TemplateContent main = new TemplateContent(MAIN, topMetricsHtml);
        return this.client.sendMessageWithTemplate(this.pulseTemplate, Collections.singletonList(main), message).thenApply(response -> response.getCode() == 200);
    }
}
