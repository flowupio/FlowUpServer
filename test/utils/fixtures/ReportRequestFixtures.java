package utils.fixtures;

import controllers.api.ReportRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface ReportRequestFixtures {

    static ReportRequest reportWithNoMetrics() {
        return new ReportRequest(
                "arbitrary_package_name",
                "arbitrary_device_model",
                "arbitrary_screen_density",
                "arbitrary_screen_size",
                "arbitrary_installation_uuid",
                2,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    static ReportRequest reportWithUIMetrics() {
        return new ReportRequest(
                "arbitrary_package_name",
                "arbitrary_device_model",
                "arbitrary_screen_density",
                "arbitrary_screen_size",
                "arbitrary_installation_uuid",
                2,
                null,
                Arrays.asList(new ReportRequest.Ui(
                        0l,
                        null,
                        null,
                        false,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null)),
                null,
                null,
                null,
                null
        );
    }

    static ReportRequest reportWithCpuMetrics(Integer numberOfMetrics) {
        List<ReportRequest.Cpu> cpuMetrics = new ArrayList<>();
        for (int i = 0; i < numberOfMetrics; i++) {
            cpuMetrics.add(new ReportRequest.Cpu(
                    i,
                    "arbitrary_app_version",
                    "arbitrary_android_version",
                    false,
                    i
            ));
        }

        return new ReportRequest(
                "arbitrary_package_name",
                "arbitrary_device_model",
                "arbitrary_screen_density",
                "arbitrary_screen_size",
                "arbitrary_installation_uuid",
                2,
                null,
                null,
                cpuMetrics,
                null,
                null,
                null
        );
    }

}
