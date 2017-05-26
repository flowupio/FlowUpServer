package controllers.api;

import org.junit.Before;
import org.junit.Test;
import usecases.models.BasicValue;
import usecases.models.DataPoint;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;

public class DataPointMapperTest {

    private static final double ANY_CPU_CONSUMPTION = 12.3;

    private DataPointMapper mapper;

    @Before
    public void setUp() {
        this.mapper = new DataPointMapper();
    }

    @Test
    public void doesNotMapCPUDataPointsIfTheAndroidAPIAssociatedIsGreaterThan19() {
        ReportRequest reportRequest = givenAReportRequestWithACPUMetric("API25", null, ANY_CPU_CONSUMPTION);

        List<DataPoint> dataPoints = mapper.mapCpu(reportRequest);

        assertTrue(dataPoints.isEmpty());
    }

    @Test
    public void mapsCPUDataPointsIfTheAndroidAPIAssociatedIsEqualTo19() {
        ReportRequest reportRequest = givenAReportRequestWithACPUMetric("API19", null, ANY_CPU_CONSUMPTION);

        List<DataPoint> dataPoints = mapper.mapCpu(reportRequest);

        assertDataPointsContainExpectedValues(dataPoints);
    }

    @Test
    public void mapsCPUDataPointsIfTheAndroidAPIAssociatedIsLowerThan19() {
        ReportRequest reportRequest = givenAReportRequestWithACPUMetric("API16", null, ANY_CPU_CONSUMPTION);

        List<DataPoint> dataPoints = mapper.mapCpu(reportRequest);

        assertDataPointsContainExpectedValues(dataPoints);
    }

    @Test
    public void mapsCPUDataPointsIfItsAnIOSAPI() {
        ReportRequest reportRequest = givenAReportRequestWithACPUMetric(null, "10.0.0", ANY_CPU_CONSUMPTION);

        List<DataPoint> dataPoints = mapper.mapCpu(reportRequest);

        assertDataPointsContainExpectedValues(dataPoints);
    }

    private void assertDataPointsContainExpectedValues(List<DataPoint> dataPoints) {
        DataPoint dataPoint = dataPoints.get(0);
        assertEquals(1, dataPoints.size());
        BasicValue consumption = (BasicValue) dataPoint.getMeasurements().get(0)._2;
        assertEquals(ANY_CPU_CONSUMPTION, consumption.getValue(), 0.1);
    }

    private ReportRequest givenAReportRequestWithACPUMetric(String androidAPI, String iosAPI, double cpuConsumption) {
        List<ReportRequest.Cpu> cpuMetrics = new LinkedList<>();
        cpuMetrics.add(new ReportRequest.Cpu(1l, "1.0.0", androidAPI, iosAPI, false, cpuConsumption));
        return new ReportRequest("io.flowup.example",
                "Nexus5",
                "hdpi",
                "1080X920",
                "UUID",
                2,
                Collections.emptyList(),
                Collections.emptyList(),
                cpuMetrics,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList());
    }

}