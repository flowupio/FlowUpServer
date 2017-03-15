package usecases;

import play.Logger;
import usecases.models.ErrorReport;

public class ReportClientError {

    public void execute(ErrorReport errorReport) {
        Logger.error("Client side exception: " + errorReport.toString());
    }
}
