package controllers;

import play.mvc.Result;
import usecases.DeleteYesterdayAllowedUUIDs;

import javax.inject.Inject;

import static play.mvc.Results.ok;

public class AllowedUUIDsController {

    private final DeleteYesterdayAllowedUUIDs deleteOldAllowedUUIDs;

    @Inject
    public AllowedUUIDsController(DeleteYesterdayAllowedUUIDs deleteOldAllowedUUIDs) {
        this.deleteOldAllowedUUIDs = deleteOldAllowedUUIDs;
    }

    public Result deleteOldAllowedUUIDs() {
        deleteOldAllowedUUIDs.execute();
        return ok();
    }

}
