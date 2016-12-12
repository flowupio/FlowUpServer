package datasources.sqs;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.*;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import play.Configuration;

import javax.inject.Named;
import java.nio.charset.Charset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class SQSClient {
    private static final int SQS_MESSAGE_MAX_LENGTH = 256 * 1024;
    public static final int MAX_NUMBER_OF_MESSAGES = 10;
    private final AmazonSQS sqs;
    private final String queueUrl;

    @Inject
    public SQSClient(@Named("sqs") Configuration sqsConf, AmazonSQS sqs) {
        this.sqs = sqs;
        Region region = RegionUtils.getRegion(sqsConf.getString("region", "eu-west-1"));
        this.sqs.setRegion(region);
        queueUrl = sqsConf.getString("queue_url");
    }

    public boolean hasMessageBodyAValidLength(String messageBody) {
        return messageBody.getBytes(Charset.defaultCharset()).length <= SQS_MESSAGE_MAX_LENGTH;
    }

    public void sendMessage(String messageBody) {
        sqs.sendMessage(new SendMessageRequest(queueUrl, messageBody));
    }

    public CompletionStage<Boolean> receiveMessages(ProcessMessage processMessage) {
        return CompletableFuture.supplyAsync(() -> {
            ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl).withMaxNumberOfMessages(MAX_NUMBER_OF_MESSAGES);
            return sqs.receiveMessage(receiveMessageRequest).getMessages();
        }).thenCompose(messages -> {
            List<String> messagesBody = messages.stream().map(Message::getBody).collect(Collectors.toList());
            return processMessage.executed(messagesBody).thenApply(processed -> {
                if (processed) {
                    List<DeleteMessageBatchRequestEntry> deleteMessageBatchRequestEntries =  messages.stream().map(Message::getReceiptHandle).map(this::getDeleteMessageBatchRequestEntry).collect(Collectors.toList());
                    sqs.deleteMessageBatch(new DeleteMessageBatchRequest(queueUrl, deleteMessageBatchRequestEntries));
                }

                return processed;
            });
        });
    }

    @NotNull
    private DeleteMessageBatchRequestEntry getDeleteMessageBatchRequestEntry(String s) {
        return new DeleteMessageBatchRequestEntry(UUID.randomUUID().toString(), s);
    }
}

