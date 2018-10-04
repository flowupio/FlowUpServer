package controllers.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SNSMessage {
    private static final String NOTIFICATION = "Notification";
    private static final String SUBSCRIPTION_CONFIRMATION = "SubscriptionConfirmation";
    private static final String UNSUBSCRIBE_CONFIRMATION = "UnsubscribeConfirmation";

    @JsonProperty("SignatureVersion")
    private String signatureVersion;
    @JsonProperty("SubscribeURL")
    private String subscribeURL;
    @JsonProperty("TopicArn")
    private String topicArn;
    @JsonProperty("Subject")
    private String subject;
    @JsonProperty("Message")
    private String message;
    @JsonProperty("MessageId")
    private String messageId;
    @JsonProperty("SigningCertURL")
    private String signingCertURL;
    @JsonProperty("Timestamp")
    private String timestamp;
    @JsonProperty("Type")
    private String type;
    @JsonProperty("Token")
    private String token;
    @JsonProperty("Signature")
    private String signature;

    private String getNotification() {
        String stringToSign;

        stringToSign = "Message\n";
        stringToSign += this.getMessage() + "\n";
        stringToSign += "MessageId\n";
        stringToSign += this.getMessageId() + "\n";
        if (this.getSubject() != null) {
            stringToSign += "Subject\n";
            stringToSign += this.getSubject() + "\n";
        }
        stringToSign += "Timestamp\n";
        stringToSign += this.getTimestamp() + "\n";
        stringToSign += "TopicArn\n";
        stringToSign += this.getTopicArn() + "\n";
        stringToSign += "Type\n";
        stringToSign += this.getType() + "\n";
        return stringToSign;
    }

    private String getSubscription() {
        String stringToSign;
        stringToSign = "Message\n";
        stringToSign += this.getMessage() + "\n";
        stringToSign += "MessageId\n";
        stringToSign += this.getMessageId() + "\n";
        stringToSign += "SubscribeURL\n";
        stringToSign += this.getSubscribeURL() + "\n";
        stringToSign += "Timestamp\n";
        stringToSign += this.getTimestamp() + "\n";
        stringToSign += "Token\n";
        stringToSign += this.getToken() + "\n";
        stringToSign += "TopicArn\n";
        stringToSign += this.getTopicArn() + "\n";
        stringToSign += "Type\n";
        stringToSign += this.getType() + "\n";
        return stringToSign;
    }

    byte [] getMessageBytes() {
        byte [] bytesToSign = null;
        if (this.getType().equals(NOTIFICATION))
            bytesToSign = this.getNotification().getBytes();
        else if (this.getType().equals(SUBSCRIPTION_CONFIRMATION) || this.getType().equals(UNSUBSCRIBE_CONFIRMATION))
            bytesToSign = this.getSubscription().getBytes();
        return bytesToSign;
    }
}
