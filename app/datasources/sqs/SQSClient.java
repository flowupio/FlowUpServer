package datasources.sqs;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.google.inject.Inject;
import play.Configuration;

import javax.inject.Named;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class SQSClient {
    private final AmazonSQS sqs;
    private String queueUrl;

    @Inject
    public SQSClient(AmazonSQS sqs, @Named("sqs") Configuration sqsConf) {
        this.sqs = sqs;
        Region region = RegionUtils.getRegion((sqsConf.getString("region", "eu-west-1"));
        sqs.setRegion(region);
    }

    public void sendMessage(String messageBody) {
        sqs.sendMessage(new SendMessageRequest(queueUrl, messageBody));
    }

    public CompletionStage<List<String>> receiveMessages() {
        return CompletableFuture.supplyAsync(() -> {
            ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
            List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
            return messages.stream().map(Message::getBody).collect(Collectors.toList());
        });
    }
}
