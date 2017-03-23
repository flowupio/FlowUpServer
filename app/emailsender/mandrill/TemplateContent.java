package emailsender.mandrill;

import lombok.Data;

@Data
public class TemplateContent {
    private final String name;
    private final String content;
}
