package controllers;

import play.mvc.Result;
import usecases.DeleteOldAllowedUUIDs;

import javax.inject.Inject;

import static play.mvc.Results.ok;

public class AllowedUUIDs {

    private final DeleteOldAllowedUUIDs deleteOldAllowedUUIDs;

    @Inject
    public AllowedUUIDs(DeleteOldAllowedUUIDs deleteOldAllowedUUIDs) {
        this.deleteOldAllowedUUIDs = deleteOldAllowedUUIDs;
    }

    public Result deleteOldAllowedUUIDs(String apiKey) {
        deleteOldAllowedUUIDs.execute(apiKey);
        return ok();
    }

}
