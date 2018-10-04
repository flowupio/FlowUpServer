package datasources.sqs;

import java.util.List;
import java.util.concurrent.CompletionStage;

public interface ProcessMessage {
    CompletionStage<Boolean> executed(List<String> messages);
}
