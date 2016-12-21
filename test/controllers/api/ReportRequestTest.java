package controllers.api;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static utils.ReportRequestMother.reportWithCpuMetrics;
import static utils.ReportRequestMother.reportWithNoMetrics;
import static utils.ReportRequestMother.reportWithUIMetrics;

public class ReportRequestTest {

    @Test
    public void whenNoCPUMetricsShouldBeConsideredDebug() throws Exception {
        assertTrue(reportWithNoMetrics().isInDebugMode());
    }

    @Test
    public void whenOneCPUMetricShouldBeConsideredDebug() throws Exception {
        assertTrue(reportWithCpuMetrics(1).isInDebugMode());
    }

    @Test
    public void whenManyCPUMetricsShouldNotBeConsideredDebug() throws Exception {
        assertFalse(reportWithCpuMetrics(2).isInDebugMode());
    }

    @Test
    public void whenNoUIMetricsShouldBeConsideredBackground() throws Exception {
        assertTrue(reportWithNoMetrics().isBackground());
    }

    @Test
    public void whenContainsUIMetricsShouldNotBeConsideredBackground() throws Exception {
        assertFalse(reportWithUIMetrics().isBackground());
    }

}