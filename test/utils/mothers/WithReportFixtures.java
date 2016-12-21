package utils.mothers;

import usecases.models.Report;

import java.util.ArrayList;

public interface WithReportFixtures {
    static Report reportWithNoAppPackage() {
        return new Report("arbitrary_api_key",
                null,
                new ArrayList<>(),
                false,
                false);
    }

    static Report reportForBackgroundDebug() {
        return new Report("arbitrary_api_key",
                "arbitrary_app_package",
                new ArrayList<>(),
                true,
                true);
    }

    static Report reportForDebug() {
        return new Report(
                "arbitrary_api_key",
                "arbitrary_app_package",
                new ArrayList<>(),
                true,
                false);
    }

    static Report reportForBackground() {
        return new Report(
                "arbitrary_api_key",
                "arbitrary_app_package",
                new ArrayList<>(),
                false,
                true);
    }

}
