package datasources.mandrill;

import lombok.Data;

@Data
public class MessagesSendTemplateResponse {
    private final String status;
    private final int code;
    private final String name;
    private final String message;
}
