package com.antigravity.util;

import com.antigravity.models.Driver;
import com.antigravity.models.RankingMethod;
import com.antigravity.models.TiebreakerMethod;
import com.antigravity.race.DriverHeatData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class GhostRaceSimulatorTest {

  private DriverHeatData createDriverHeatData(String id, double... lapTimes) {
    DriverHeatData dhd = new DriverHeatData();
    Driver actualDriver = new Driver(id, id, id, id);
    dhd.setActualDriver(actualDriver);
    List<DriverHeatData.LapData> laps = new ArrayList<>();
    for (double t : lapTimes) {
      DriverHeatData.LapData lap = new DriverHeatData.LapData();
      lap.setLapTime(t);
      laps.add(lap);
    }
    dhd.setLaps(laps);
    return dhd;
  }

  @Test
  public void testSimulatorInstantiation() {
    GhostRaceSimulator simulator = new GhostRaceSimulator();
    Assert.assertNotNull(simulator);
  }

  @Test
  public void testGhostRace() {
    DriverHeatData a =
        createDriverHeatData(
            "A", 10.0, 10.0, 10.0, 10.0, 10.0); // finishes laps at 10, 20, 30, 40, 50
    DriverHeatData b =
        createDriverHeatData(
            "B", 11.0, 11.0, 11.0, 11.0, 5.0); // finishes laps at 11, 22, 33, 44, 49

    Map<String, Integer> led =
        GhostRaceSimulator.calculateLapsLed(
            Arrays.asList(a, b), RankingMethod.LAP_COUNT, TiebreakerMethod.TOTAL_TIME);

    Assert.assertEquals(4, (int) led.get("A"));
    Assert.assertEquals(1, (int) led.get("B"));
  }

  @Test
  public void testGhostRace_FastestLapRanking() {
    DriverHeatData a = createDriverHeatData("A", 10.0, 9.0, 8.0);
    DriverHeatData b = createDriverHeatData("B", 9.5, 7.5, 9.0);

    Map<String, Integer> led =
        GhostRaceSimulator.calculateLapsLed(
            Arrays.asList(a, b), RankingMethod.FASTEST_LAP, TiebreakerMethod.TOTAL_TIME);

    Assert.assertEquals(0, (int) led.get("A"));
    Assert.assertEquals(3, (int) led.get("B"));
  }

  @Test
  public void testGhostRace_AverageLapRanking() {
    DriverHeatData a = createDriverHeatData("A", 5.0, 5.0);
    DriverHeatData b = createDriverHeatData("B", 6.0, 6.0);

    Map<String, Integer> led =
        GhostRaceSimulator.calculateLapsLed(
            Arrays.asList(a, b), RankingMethod.AVERAGE_LAP, TiebreakerMethod.FASTEST_LAP_TIME);

    Assert.assertEquals(2, (int) led.get("A"));
    Assert.assertEquals(0, (int) led.get("B"));
  }

  @Test
  public void testGhostRace_TotalTimeRanking() {
    DriverHeatData a = createDriverHeatData("A", 4.0, 4.0);
    DriverHeatData b = createDriverHeatData("B", 5.0, 5.0);

    Map<String, Integer> led =
        GhostRaceSimulator.calculateLapsLed(
            Arrays.asList(a, b), RankingMethod.TOTAL_TIME, TiebreakerMethod.FASTEST_LAP_TIME);

    Assert.assertEquals(2, (int) led.get("A"));
    Assert.assertEquals(0, (int) led.get("B"));
  }

  @Test
  public void testGhostRace_MedianLapTiebreaker() {
    DriverHeatData a = createDriverHeatData("A", 5.0, 5.0, 5.0);
    DriverHeatData b = createDriverHeatData("B", 5.0, 5.0, 5.0);

    Map<String, Integer> led =
        GhostRaceSimulator.calculateLapsLed(
            Arrays.asList(a, b), RankingMethod.LAP_COUNT, TiebreakerMethod.MEDIAN_LAP_TIME);

    Assert.assertNotNull(led);
  }

  @Test
  public void testTotalLapsLedEqualsMaxLaps() {
    DriverHeatData d2 = createDriverHeatData("d2", 3.098, 4.500, 3.840, 3.098);
    DriverHeatData d4 = createDriverHeatData("d4", 3.547, 3.547, 4.106, 3.547);
    DriverHeatData d1 = createDriverHeatData("d1", 3.151, 4.052, 4.300, 3.151);
    DriverHeatData d3 = createDriverHeatData("d3", 3.496, 4.114, 3.496, 4.699);

    Map<String, Integer> led =
        GhostRaceSimulator.calculateLapsLed(
            Arrays.asList(d2, d4, d1, d3),
            RankingMethod.LAP_COUNT,
            TiebreakerMethod.AVERAGE_LAP_TIME);

    int totalLapsLed = led.values().stream().mapToInt(Integer::intValue).sum();
    Assert.assertEquals(4, totalLapsLed);
  }

  @Test
  public void testEmptyAndNullInputs() {
    Assert.assertTrue(
        GhostRaceSimulator.calculateLapsLed(
                null, RankingMethod.LAP_COUNT, TiebreakerMethod.AVERAGE_LAP_TIME)
            .isEmpty());
    Assert.assertTrue(
        GhostRaceSimulator.calculateLapsLed(
                new ArrayList<>(), RankingMethod.LAP_COUNT, TiebreakerMethod.AVERAGE_LAP_TIME)
            .isEmpty());
    Map<String, Integer> zeroLaps =
        GhostRaceSimulator.calculateLapsLed(
            Collections.singletonList(createDriverHeatData("d1")),
            RankingMethod.LAP_COUNT,
            TiebreakerMethod.AVERAGE_LAP_TIME);
    Assert.assertEquals(1, zeroLaps.size());
    Assert.assertEquals(0, (int) zeroLaps.get("d1"));
  }

  @Test
  public void testSingleDriverLeadsAllLaps() {
    DriverHeatData d = createDriverHeatData("d1", 5.0, 5.0, 5.0);
    Map<String, Integer> led =
        GhostRaceSimulator.calculateLapsLed(
            Arrays.asList(d), RankingMethod.LAP_COUNT, TiebreakerMethod.AVERAGE_LAP_TIME);
    Assert.assertEquals(1, led.size());
    Assert.assertEquals(3, (int) led.get("d1"));
  }

  @Test
  public void testEmptyLanesIgnored() {
    DriverHeatData d = createDriverHeatData("d1", 5.0, 5.0);
    DriverHeatData empty = new DriverHeatData();
    empty.setActualDriver(Driver.EMPTY_DRIVER);

    Map<String, Integer> led =
        GhostRaceSimulator.calculateLapsLed(
            Arrays.asList(d, empty), RankingMethod.LAP_COUNT, TiebreakerMethod.AVERAGE_LAP_TIME);
    Assert.assertEquals(1, led.size());
    Assert.assertEquals(2, (int) led.get("d1"));
  }

  @Test
  public void testGetDriverId() {
    Assert.assertNull(GhostRaceSimulator.getDriverId(null));

    DriverHeatData dhd = new DriverHeatData();
    dhd.setActualDriver(new Driver("drv123", "Nick", "id1", ""));
    Assert.assertEquals("id1", GhostRaceSimulator.getDriverId(dhd));
  }

  @Test
  public void testLapPerformanceSnapshotAccessors() {
    GhostRaceSimulator.LapPerformanceSnapshot snapshot =
        new GhostRaceSimulator.LapPerformanceSnapshot("p1", 10, 55.5, 4.8, 5.55, 5.5, false, 42);

    Assert.assertEquals("p1", snapshot.getParticipantId());
    Assert.assertEquals(10.0, snapshot.getAdjustedLapCount(), 0.001);
    Assert.assertEquals(55.5, snapshot.getTotalTime(), 0.001);
    Assert.assertEquals(4.8, snapshot.getBestLapTime(), 0.001);
    Assert.assertEquals(5.55, snapshot.getAverageLapTime(), 0.001);
    Assert.assertEquals(5.5, snapshot.getMedianLapTime(), 0.001);
    Assert.assertFalse(snapshot.isEmptyParticipant());
    Assert.assertEquals(42, snapshot.getSeed());
  }

  @Test
  public void testCalculateLapsLedWithZeroOrNegativeLapTimes() {
    DriverHeatData a = createDriverHeatData("A", 0.0, -1.0, 5.0);
    DriverHeatData b = createDriverHeatData("B", 6.0, 6.0, 6.0);

    Map<String, Integer> led =
        GhostRaceSimulator.calculateLapsLed(
            Arrays.asList(a, b), RankingMethod.FASTEST_LAP, TiebreakerMethod.TOTAL_TIME);
    Assert.assertNotNull(led);
  }

  @Test
  public void testCalculateGhostGap_Ahead() {
    // 50% through lap. Ghost lap time is 10.0s (expected 5.0s elapsed).
    // Live driver elapsed is 4.2s (faster by 0.8s).
    GhostRaceSimulator.GhostGapResult result = GhostRaceSimulator.calculateGhostGap(0.5, 4.2, 10.0);

    Assert.assertTrue(result.isAhead());
    Assert.assertEquals(0.8, result.getDeltaSeconds(), 0.001);
    Assert.assertEquals(0.5, result.getProgressPct(), 0.001);
    Assert.assertEquals(8.4, result.getLiveProjectedLapTime(), 0.001);
    Assert.assertEquals(10.0, result.getGhostLapTime(), 0.001);
  }

  @Test
  public void testCalculateGhostGap_Behind() {
    // 60% through lap. Ghost lap time is 8.0s (expected 4.8s elapsed).
    // Live driver elapsed is 5.4s (slower by 0.6s).
    GhostRaceSimulator.GhostGapResult result = GhostRaceSimulator.calculateGhostGap(0.6, 5.4, 8.0);

    Assert.assertFalse(result.isAhead());
    Assert.assertEquals(-0.6, result.getDeltaSeconds(), 0.001);
    Assert.assertEquals(0.6, result.getProgressPct(), 0.001);
    Assert.assertEquals(9.0, result.getLiveProjectedLapTime(), 0.001);
  }

  @Test
  public void testCalculateGhostGap_EdgeCases() {
    // Zero / negative ghost lap time
    GhostRaceSimulator.GhostGapResult res1 = GhostRaceSimulator.calculateGhostGap(0.5, 3.0, 0.0);
    Assert.assertEquals(0.0, res1.getDeltaSeconds(), 0.001);

    // Negative elapsed time
    GhostRaceSimulator.GhostGapResult res2 = GhostRaceSimulator.calculateGhostGap(0.5, -1.0, 10.0);
    Assert.assertEquals(0.0, res2.getDeltaSeconds(), 0.001);

    // Progress at zero
    GhostRaceSimulator.GhostGapResult res3 = GhostRaceSimulator.calculateGhostGap(0.0, 0.0, 10.0);
    Assert.assertEquals(0.0, res3.getDeltaSeconds(), 0.001);

    // Progress clamping (e.g. > 1.0)
    GhostRaceSimulator.GhostGapResult res4 = GhostRaceSimulator.calculateGhostGap(1.5, 10.0, 10.0);
    Assert.assertEquals(1.0, res4.getProgressPct(), 0.001);
    Assert.assertEquals(0.0, res4.getDeltaSeconds(), 0.001);
  }

  @Test
  public void testGenerateGhostLapSeries() {
    List<Double> base = Arrays.asList(5.0, 5.0, 5.0, 5.0);
    // Pace multiplier: 1.10 (+10%), Degradation: 0.10s per lap, Pit stop on lap 3: 4.0s
    List<GhostRaceSimulator.SimulatedLap> laps =
        GhostRaceSimulator.generateGhostLapSeries(base, 1.10, 0.10, 4.0, 3);

    Assert.assertEquals(4, laps.size());

    // Lap 1: (5.0 * 1.10) + 0.0 + 0 = 5.5
    Assert.assertEquals(1, laps.get(0).getLapNumber());
    Assert.assertEquals(5.5, laps.get(0).getLapTime(), 0.001);
    Assert.assertEquals(5.5, laps.get(0).getCumulativeTime(), 0.001);
    Assert.assertFalse(laps.get(0).isPitLap());

    // Lap 2: (5.0 * 1.10) + 0.10 + 0 = 5.6
    Assert.assertEquals(2, laps.get(1).getLapNumber());
    Assert.assertEquals(5.6, laps.get(1).getLapTime(), 0.001);
    Assert.assertEquals(11.1, laps.get(1).getCumulativeTime(), 0.001);

    // Lap 3: (5.0 * 1.10) + 0.20 + 4.0 = 9.7 (Pit lap)
    Assert.assertEquals(3, laps.get(2).getLapNumber());
    Assert.assertEquals(9.7, laps.get(2).getLapTime(), 0.001);
    Assert.assertEquals(20.8, laps.get(2).getCumulativeTime(), 0.001);
    Assert.assertTrue(laps.get(2).isPitLap());

    // Lap 4: (5.0 * 1.10) + 0.30 = 5.8
    Assert.assertEquals(4, laps.get(3).getLapNumber());
    Assert.assertEquals(5.8, laps.get(3).getLapTime(), 0.001);
    Assert.assertEquals(26.6, laps.get(3).getCumulativeTime(), 0.001);
  }

  @Test
  public void testGenerateGhostLapSeries_EmptyAndDefaults() {
    Assert.assertTrue(GhostRaceSimulator.generateGhostLapSeries(null, 1.0, 0.0, 0.0, 0).isEmpty());
    Assert.assertTrue(
        GhostRaceSimulator.generateGhostLapSeries(Collections.emptyList(), 1.0, 0.0, 0.0, 0)
            .isEmpty());

    // Zero / negative paceMultiplier defaults to 1.0
    List<GhostRaceSimulator.SimulatedLap> laps =
        GhostRaceSimulator.generateGhostLapSeries(Arrays.asList(4.0, null), 0.0, 0.0, 0.0, 0);
    Assert.assertEquals(2, laps.size());
    Assert.assertEquals(4.0, laps.get(0).getLapTime(), 0.001);
    Assert.assertEquals(0.0, laps.get(1).getLapTime(), 0.001);
  }

  @Test
  public void testCompareHeatTrajectories() {
    // Driver A: 4 laps at [4.0, 4.5, 4.0, 4.0] (Cumulative: 4.0, 8.5, 12.5, 16.5)
    // Driver B: 4 laps at [4.2, 4.0, 4.5, 3.5] (Cumulative: 4.2, 8.2, 12.7, 16.2)
    // Lap 1: A=4.0, B=4.2 -> Delta = +0.2 (A leads)
    // Lap 2: A=8.5, B=8.2 -> Delta = -0.3 (B leads -> 1st lead change)
    // Lap 3: A=12.5, B=12.7 -> Delta = +0.2 (A leads -> 2nd lead change)
    // Lap 4: A=16.5, B=16.2 -> Delta = -0.3 (B leads -> 3rd lead change, B wins by 0.3s)
    DriverHeatData a = createDriverHeatData("A", 4.0, 4.5, 4.0, 4.0);
    DriverHeatData b = createDriverHeatData("B", 4.2, 4.0, 4.5, 3.5);

    GhostRaceSimulator.HeatTrajectoryComparison comp =
        GhostRaceSimulator.compareHeatTrajectories(a, b);

    Assert.assertEquals("A", comp.getDriverIdA());
    Assert.assertEquals("B", comp.getDriverIdB());
    Assert.assertEquals(4, comp.getCommonLaps());
    Assert.assertEquals(4, comp.getCumulativeDeltas().size());
    Assert.assertEquals(3, comp.getLeadChanges());
    Assert.assertEquals("B", comp.getLeaderAtFinish());
    Assert.assertEquals(-0.3, comp.getFinalDelta(), 0.001);
    Assert.assertEquals(0.2, comp.getMaxAdvantageA(), 0.001);
    Assert.assertEquals(0.3, comp.getMaxAdvantageB(), 0.001);
  }

  @Test
  public void testCompareHeatTrajectories_EdgeCases() {
    // Null inputs
    GhostRaceSimulator.HeatTrajectoryComparison nullComp =
        GhostRaceSimulator.compareHeatTrajectories(null, null);
    Assert.assertEquals(0, nullComp.getCommonLaps());

    // 0 Laps
    DriverHeatData a = createDriverHeatData("A");
    DriverHeatData b = createDriverHeatData("B");
    GhostRaceSimulator.HeatTrajectoryComparison zeroComp =
        GhostRaceSimulator.compareHeatTrajectories(a, b);
    Assert.assertEquals(0, zeroComp.getCommonLaps());

    // Dead Heat Tie
    DriverHeatData tieA = createDriverHeatData("A", 5.0, 5.0);
    DriverHeatData tieB = createDriverHeatData("B", 5.0, 5.0);
    GhostRaceSimulator.HeatTrajectoryComparison tieComp =
        GhostRaceSimulator.compareHeatTrajectories(tieA, tieB);
    Assert.assertEquals("TIE", tieComp.getLeaderAtFinish());
    Assert.assertEquals(0.0, tieComp.getFinalDelta(), 0.001);
  }
}
