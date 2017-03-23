package emailsender.mandrill;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import models.Application;
import models.User;
import org.joda.time.DateTime;
import play.Configuration;
import emailsender.EmailSender;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class MandrillSender implements EmailSender {
    private static final String COMPANY = "COMPANY";
    private static final String APP_PACKAGE = "APP_PACKAGE";
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
    private final Boolean pulseDryRun;
    private final String pulseDryRunEmail;
    private final DateTimeFormatter dateFormat;
    private final String firstReportReceivedTemplate;
    private final String firstReportReceivedSubject;
    private final Var[] globalMergeVars;

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
        this.pulseDryRun = configuration.getBoolean("pulse.dry_run", true);
        this.pulseDryRunEmail = configuration.getString("pulse.dry_run_email", "tech@flowup.io");

        this.firstReportReceivedTemplate = configuration.getString("first_report_received.template");
        this.firstReportReceivedSubject = configuration.getString("first_report_received.subject");

        this.dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG);
        this.globalMergeVars = new Var[]{new Var(COMPANY, companyName)};
    }

    @Override
    public CompletionStage<Boolean> sendSigningUpDisabledMessage(User user) {
        List<Recipient> recipient = extractUserEmails(Collections.singletonList(user));
        Message message = new Message(
                signingUpDisabledSubject,
                fromEmail,
                fromName,
                recipient,
                globalMergeVars);
        return this.client.sendMessageWithTemplate(this.signingUpDisabledTemplate, message).thenApply(response -> response.getCode() == 200);
    }

    @Override
    public CompletionStage<Boolean> sendSignUpApprovedMessage(User user) {
        List<Recipient> recipient = extractUserEmails(Collections.singletonList(user));
        Message message = new Message(
                signUpApprovedSubject,
                fromEmail,
                fromName,
                recipient,
                globalMergeVars);
        return this.client.sendMessageWithTemplate(this.signUpApprovedTemplate, message).thenApply(response -> response.getCode() == 200);
    }

    @Override
    public CompletionStage<Boolean> sendKeyMetricsMessage(List<User> users, String appPackage, ZonedDateTime dateTime, String topMetricsHtml) {
        List<Recipient> recipients = extractUserEmails(users);
        Message message = new Message(
                String.format(pulseSubject, appPackage, dateTime.format(dateFormat)),
                fromEmail,
                fromName,
                recipients,
                globalMergeVars);
        TemplateContent main = new TemplateContent(MAIN, topMetricsHtml);
        return this.client.sendMessageWithTemplate(this.pulseTemplate, Collections.singletonList(main), message).thenApply(response -> response.getCode() == 200);
    }

    @Override
    public CompletionStage<Boolean> sendFirstReportReceived(Application app) {
        List<User> users = app.getOrganization().getMembers();
        List<Recipient> recipients = extractUserEmails(users);
        Var[] vars = appendVar(new Var(APP_PACKAGE, app.getAppPackage()));
        Message message = new Message(
                firstReportReceivedSubject,
                fromEmail,
                fromName,
                recipients,
                vars);
        return this.client.sendMessageWithTemplate(firstReportReceivedTemplate, message).thenApply(response -> response.getCode() == 200);
    }

    private Var[] appendVar(Var var) {
        Var[] vars = new Var[globalMergeVars.length + 1];
        vars[vars.length - 1] = var;
        return vars;
    }

    private List<Recipient> extractUserEmails(List<User> users) {
        return users.stream().map(user -> {
                    String email;
                    if (this.pulseDryRun) {
                        email = this.pulseDryRunEmail;
                    } else {
                        email = user.getEmail();
                    }
                    return new Recipient(email, user.getName(), TO);
                }
        ).collect(Collectors.toList());
    }
}
