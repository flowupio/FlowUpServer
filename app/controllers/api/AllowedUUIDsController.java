package controllers.api;

import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import usecases.DeleteYesterdayAllowedUUIDs;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class AllowedUUIDsController extends Controller {

    private final DeleteYesterdayAllowedUUIDs deleteOldAllowedUUIDs;

    @Inject
    public AllowedUUIDsController(DeleteYesterdayAllowedUUIDs deleteOldAllowedUUIDs) {
        this.deleteOldAllowedUUIDs = deleteOldAllowedUUIDs;
    }

    @BodyParser.Of(SNSMessageBodyParser.class)
    public CompletionStage<Result> deleteOldAllowedUUIDs() {
        SNSMessage message = request().body().as(SNSMessage.class);
//        message.getSubject();
//        message.getMessage();

        return CompletableFuture.supplyAsync(() -> {
            deleteOldAllowedUUIDs.execute();
            return ok();
        });
    }

}
