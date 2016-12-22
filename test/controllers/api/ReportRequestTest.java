package controllers.api;

import org.junit.Test;
import utils.WithResources;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ReportRequestTest implements WithResources {

    @Test
    public void whenNoCPUMetricsShouldBeConsideredDebug() throws Exception {
        ReportRequest report = resourceFromFile("androidsdk/noMetricsReportRequestBody.json", ReportRequest.class);
        assertTrue(report.isInDebugMode());
    }

    @Test
    public void whenOneCPUMetricShouldBeConsideredDebug() throws Exception {
        ReportRequest report = resourceFromFile("androidsdk/oneCPUMetricReportRequestBody.json", ReportRequest.class);
        assertTrue(report.isInDebugMode());
    }

    @Test
    public void whenManyCPUMetricsShouldNotBeConsideredDebug() throws Exception {
        ReportRequest report = resourceFromFile("androidsdk/multipleCPUMetricReportRequestBody.json", ReportRequest.class);
        assertFalse(report.isInDebugMode());
    }

    @Test
    public void whenNoUIMetricsShouldBeConsideredBackground() throws Exception {
        ReportRequest report = resourceFromFile("androidsdk/noMetricsReportRequestBody.json", ReportRequest.class);
        assertTrue(report.isBackground());
    }

    @Test
    public void whenContainsUIMetricsShouldNotBeConsideredBackground() throws Exception {
        ReportRequest report = resourceFromFile("androidsdk/oneUIMetricReportRequestBody.json", ReportRequest.class);
        assertFalse(report.isBackground());
    }

}