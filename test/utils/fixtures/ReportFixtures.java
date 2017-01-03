package utils.fixtures;

import usecases.models.Report;

import java.util.ArrayList;

public interface ReportFixtures {
    static Report reportWithNoAppPackage() {
        return new Report("arbitrary_api_key",
                null,
                new ArrayList<>(),
                new Report.Metadata(false, false));
    }

    static Report reportWithEmptyAppPackage() {
        return new Report("arbitrary_api_key",
                "    ",
                new ArrayList<>(),
                new Report.Metadata(false, false));
    }

    static Report reportForBackgroundDebug() {
        return new Report("arbitrary_api_key",
                "arbitrary_app_package",
                new ArrayList<>(),
                new Report.Metadata(true, true));
    }

    static Report reportForDebug() {
        return new Report(
                "arbitrary_api_key",
                "arbitrary_app_package",
                new ArrayList<>(),
                new Report.Metadata(true, false));
    }

    static Report reportForBackground() {
        return new Report(
                "arbitrary_api_key",
                "arbitrary_app_package",
                new ArrayList<>(),
                new Report.Metadata(false, true));
    }

}