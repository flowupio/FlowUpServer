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
    private static final int MAX_NUMBER_OF_MESSAGES = 10;
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
            List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
            if (messages.size() == MAX_NUMBER_OF_MESSAGES) {
                List<Message> messages2 = sqs.receiveMessage(receiveMessageRequest).getMessages();
                messages.addAll(messages2);
            }
            return messages;
        }).thenCompose(messages -> {
            List<String> messagesBody = messages.stream().map(Message::getBody).collect(Collectors.toList());
            return processMessage.executed(messagesBody).thenApply(processed -> {
                if (processed) {
                    if (!messages.isEmpty()) {
                        List<DeleteMessageBatchRequestEntry> deleteMessageBatchRequestEntries =  messages.stream().map(Message::getReceiptHandle).map(this::getDeleteMessageBatchRequestEntry).collect(Collectors.toList());

                        if (deleteMessageBatchRequestEntries.size() > MAX_NUMBER_OF_MESSAGES) {
                            List<DeleteMessageBatchRequestEntry> deleteMessageBatchRequestEntries1 = deleteMessageBatchRequestEntries.subList(0, MAX_NUMBER_OF_MESSAGES);
                            sqs.deleteMessageBatch(new DeleteMessageBatchRequest(queueUrl, deleteMessageBatchRequestEntries1));

                            List<DeleteMessageBatchRequestEntry> deleteMessageBatchRequestEntries2 = deleteMessageBatchRequestEntries.subList(MAX_NUMBER_OF_MESSAGES, deleteMessageBatchRequestEntries.size());
                            sqs.deleteMessageBatch(new DeleteMessageBatchRequest(queueUrl, deleteMessageBatchRequestEntries2));
                        } else {
                            sqs.deleteMessageBatch(new DeleteMessageBatchRequest(queueUrl, deleteMessageBatchRequestEntries));
                        }
                    }
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

