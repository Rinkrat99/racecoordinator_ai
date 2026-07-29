package com.antigravity.race;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class DriverAnalysisSummaryTest {

  @Test
  public void testDriverAnalysisSummaryGettersAndSetters() {
    DriverAnalysisSummary summary = new DriverAnalysisSummary();
    summary.setDriverName("Lotus 98T #12");
    summary.setDriverId("driver-1");

    assertEquals("Lotus 98T #12", summary.getDriverName());
    assertEquals("driver-1", summary.getDriverId());

    DriverAnalysisSummary.LaneStats stats1 =
        new DriverAnalysisSummary.LaneStats(
            "Lane 1", 1, 30.99, 172.564, 5.752, 5.548, 5.481, 0.672, 0.9528, 5.488, 5.500, 5.513,
            10.977, 16.479);

    summary.addLaneStats(stats1);
    assertEquals(1, summary.getLaneStats().size());

    DriverAnalysisSummary.LaneStats fetched = summary.getLaneStats().get(0);
    assertEquals("Lane 1", fetched.getLaneName());
    assertEquals(1, fetched.getLaneNumber());
    assertEquals(30.99, fetched.getTotalLaps(), 0.0001);
    assertEquals(172.564, fetched.getTotalTime(), 0.0001);
    assertEquals(5.752, fetched.getAverageLapTime(), 0.0001);
    assertEquals(5.548, fetched.getMedianLapTime(), 0.0001);
    assertEquals(5.481, fetched.getBestLapTime(), 0.0001);
    assertEquals(0.672, fetched.getStandardDeviation(), 0.0001);
    assertEquals(0.9528, fetched.getConsistencyScore(), 0.0001);
    assertEquals(5.488, fetched.getAverageTop5(), 0.0001);
    assertEquals(5.500, fetched.getAverageTop10(), 0.0001);
    assertEquals(5.513, fetched.getAverageTop15(), 0.0001);
    assertEquals(10.977, fetched.getTop2Consecutive(), 0.0001);
    assertEquals(16.479, fetched.getTop3Consecutive(), 0.0001);
  }

  @Test
  public void testLaneStatsSetters() {
    DriverAnalysisSummary.LaneStats stats = new DriverAnalysisSummary.LaneStats();
    stats.setLaneName("Lane 2");
    stats.setLaneNumber(2);
    stats.setTotalLaps(25.0);
    stats.setTotalTime(150.0);
    stats.setAverageLapTime(6.0);
    stats.setMedianLapTime(5.9);
    stats.setBestLapTime(5.5);
    stats.setStandardDeviation(0.5);
    stats.setConsistencyScore(0.916);
    stats.setAverageTop5(5.6);
    stats.setAverageTop10(5.7);
    stats.setAverageTop15(5.8);
    stats.setTop2Consecutive(11.2);
    stats.setTop3Consecutive(17.1);

    assertEquals("Lane 2", stats.getLaneName());
    assertEquals(2, stats.getLaneNumber());
    assertEquals(25.0, stats.getTotalLaps(), 0.001);
    assertEquals(150.0, stats.getTotalTime(), 0.001);
    assertEquals(6.0, stats.getAverageLapTime(), 0.001);
    assertEquals(5.9, stats.getMedianLapTime(), 0.001);
    assertEquals(5.5, stats.getBestLapTime(), 0.001);
    assertEquals(0.5, stats.getStandardDeviation(), 0.001);
    assertEquals(0.916, stats.getConsistencyScore(), 0.001);
    assertEquals(5.6, stats.getAverageTop5(), 0.001);
    assertEquals(5.7, stats.getAverageTop10(), 0.001);
    assertEquals(5.8, stats.getAverageTop15(), 0.001);
    assertEquals(11.2, stats.getTop2Consecutive(), 0.001);
    assertEquals(17.1, stats.getTop3Consecutive(), 0.001);
  }

  @Test
  public void testNullLaneStatsListHandling() {
    DriverAnalysisSummary summary = new DriverAnalysisSummary("Test Driver", "id-123");
    summary.setLaneStats(null);
    assertNotNull(summary.getLaneStats());
    assertEquals(0, summary.getLaneStats().size());
  }
}
