package controllers.api;

import lombok.Data;

@Data
public class SNSMessage {
    private static final String NOTIFICATION = "Notification";
    private static final String SUBSCRIPTION_CONFIRMATION = "SubscriptionConfirmation";
    private static final String UNSUBSCRIBE_CONFIRMATION = "UnsubscribeConfirmation";

    private String signatureVersion;
    private String subscribeURL;
    private String topicArn;
    private String subject;
    private String message;
    private String messageId;
    private String signingCertURL;
    private String timestamp;
    private String type;
    private String token;
    private String signature;

    private String getNotification() {
        String stringToSign;

        stringToSign = "SNSMessage\n";
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
        stringToSign = "SNSMessage\n";
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

    public byte [] getMessageBytes() {
        byte [] bytesToSign = null;
        if (this.getType().equals(NOTIFICATION))
            bytesToSign = this.getNotification().getBytes();
        else if (this.getType().equals(SUBSCRIPTION_CONFIRMATION) || this.getType().equals(UNSUBSCRIBE_CONFIRMATION))
            bytesToSign = this.getSubscription().getBytes();
        return bytesToSign;
    }
}
