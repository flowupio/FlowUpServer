package emailsender.mandrill;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Message {
    private final String subject;
    @JsonProperty("from_email")
    private final String fromEmail;
    @JsonProperty("from_name")
    private final String fromName;
    private final List<Recipient> to;
    @JsonProperty("global_merge_vars")
    private final Var[] globalMergeVars;
}

@Data
class Var {
    private final String name;
    private final String content;
}

@Data
class Recipient {
    private final String email;
    private final String name;
    private final String type;
}
