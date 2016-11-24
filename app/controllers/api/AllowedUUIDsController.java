package controllers.api;

import play.mvc.Controller;
import play.mvc.Result;
import usecases.DeleteYesterdayAllowedUUIDs;

import javax.inject.Inject;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import static play.mvc.Results.ok;

public class AllowedUUIDsController extends Controller {

    private final DeleteYesterdayAllowedUUIDs deleteOldAllowedUUIDs;

    @Inject
    public AllowedUUIDsController(DeleteYesterdayAllowedUUIDs deleteOldAllowedUUIDs) {
        this.deleteOldAllowedUUIDs = deleteOldAllowedUUIDs;
    }

    public CompletionStage<Result> deleteOldAllowedUUIDs() {
        return CompletableFuture.supplyAsync(() -> {
            deleteOldAllowedUUIDs.execute();
            return ok();
        });
    }

}
