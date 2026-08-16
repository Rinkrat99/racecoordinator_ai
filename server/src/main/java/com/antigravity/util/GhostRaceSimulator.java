package com.antigravity.util;

import com.antigravity.models.RankingMethod;
import com.antigravity.models.TiebreakerMethod;
import com.antigravity.race.DriverHeatData;
import com.antigravity.race.StandingsComparator;
import com.antigravity.race.StandingsParticipant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility for historical ghost race simulation, real-time ghost gap telemetry calculations,
 * simulated pace projections, and multi-driver heat trajectory comparisons.
 */
public class GhostRaceSimulator {

  /** Snapshot representing performance metrics for standings calculation at a specific lap. */
  public static class LapPerformanceSnapshot implements StandingsParticipant {
    private final String participantId;
    private final int lapCount;
    private final double totalTime;
    private final double bestLapTime;
    private final double averageLapTime;
    private final double medianLapTime;
    private final boolean emptyParticipant;
    private final int seed;

    public LapPerformanceSnapshot(
        String participantId,
        int lapCount,
        double totalTime,
        double bestLapTime,
        double averageLapTime,
        double medianLapTime,
        boolean emptyParticipant,
        int seed) {
      this.participantId = participantId;
      this.lapCount = lapCount;
      this.totalTime = totalTime;
      this.bestLapTime = bestLapTime;
      this.averageLapTime = averageLapTime;
      this.medianLapTime = medianLapTime;
      this.emptyParticipant = emptyParticipant;
      this.seed = seed;
    }

    @Override
    public double getAdjustedLapCount() {
      return lapCount;
    }

    @Override
    public double getTotalTime() {
      return totalTime;
    }

    @Override
    public double getBestLapTime() {
      return bestLapTime;
    }

    @Override
    public double getAverageLapTime() {
      return averageLapTime;
    }

    @Override
    public double getMedianLapTime() {
      return medianLapTime;
    }

    @Override
    public boolean isEmptyParticipant() {
      return emptyParticipant;
    }

    @Override
    public int getSeed() {
      return seed;
    }

    @Override
    public String getParticipantId() {
      return participantId;
    }
  }

  /** Result of an interpolated ghost gap calculation. */
  public static class GhostGapResult {
    private final double deltaSeconds;
    private final double progressPct;
    private final double liveProjectedLapTime;
    private final double ghostLapTime;

    public GhostGapResult(
        double deltaSeconds, double progressPct, double liveProjectedLapTime, double ghostLapTime) {
      this.deltaSeconds = deltaSeconds;
      this.progressPct = progressPct;
      this.liveProjectedLapTime = liveProjectedLapTime;
      this.ghostLapTime = ghostLapTime;
    }

    /**
     * Delta in seconds between ghost benchmark and live driver. Positive value indicates the live
     * driver is ahead of the ghost pace.
     */
    public double getDeltaSeconds() {
      return deltaSeconds;
    }

    /** Lap progress ratio (0.0 to 1.0). */
    public double getProgressPct() {
      return progressPct;
    }

    /** Projected full lap time for the live driver at current pace. */
    public double getLiveProjectedLapTime() {
      return liveProjectedLapTime;
    }

    /** Reference ghost lap time. */
    public double getGhostLapTime() {
      return ghostLapTime;
    }

    /** Returns true if live driver is currently ahead of ghost pace. */
    public boolean isAhead() {
      return deltaSeconds >= 0;
    }
  }

  /** Simulated individual lap telemetry. */
  public static class SimulatedLap {
    private final int lapNumber;
    private final double lapTime;
    private final double cumulativeTime;
    private final boolean isPitLap;

    public SimulatedLap(int lapNumber, double lapTime, double cumulativeTime, boolean isPitLap) {
      this.lapNumber = lapNumber;
      this.lapTime = lapTime;
      this.cumulativeTime = cumulativeTime;
      this.isPitLap = isPitLap;
    }

    public int getLapNumber() {
      return lapNumber;
    }

    public double getLapTime() {
      return lapTime;
    }

    public double getCumulativeTime() {
      return cumulativeTime;
    }

    public boolean isPitLap() {
      return isPitLap;
    }
  }

  /** Detailed trajectory comparison between two drivers in a heat. */
  public static class HeatTrajectoryComparison {
    private final String driverIdA;
    private final String driverIdB;
    private final int commonLaps;
    private final List<Double> cumulativeDeltas;
    private final int leadChanges;
    private final String leaderAtFinish;
    private final double finalDelta;
    private final double maxAdvantageA;
    private final double maxAdvantageB;

    public HeatTrajectoryComparison(
        String driverIdA,
        String driverIdB,
        int commonLaps,
        List<Double> cumulativeDeltas,
        int leadChanges,
        String leaderAtFinish,
        double finalDelta,
        double maxAdvantageA,
        double maxAdvantageB) {
      this.driverIdA = driverIdA;
      this.driverIdB = driverIdB;
      this.commonLaps = commonLaps;
      this.cumulativeDeltas = cumulativeDeltas;
      this.leadChanges = leadChanges;
      this.leaderAtFinish = leaderAtFinish;
      this.finalDelta = finalDelta;
      this.maxAdvantageA = maxAdvantageA;
      this.maxAdvantageB = maxAdvantageB;
    }

    public String getDriverIdA() {
      return driverIdA;
    }

    public String getDriverIdB() {
      return driverIdB;
    }

    public int getCommonLaps() {
      return commonLaps;
    }

    public List<Double> getCumulativeDeltas() {
      return cumulativeDeltas;
    }

    public int getLeadChanges() {
      return leadChanges;
    }

    public String getLeaderAtFinish() {
      return leaderAtFinish;
    }

    public double getFinalDelta() {
      return finalDelta;
    }

    public double getMaxAdvantageA() {
      return maxAdvantageA;
    }

    public double getMaxAdvantageB() {
      return maxAdvantageB;
    }
  }

  public static String getDriverId(DriverHeatData dhd) {
    if (dhd == null) return null;
    return dhd.getParticipantId();
  }

  /**
   * Interpolates real-time gap between live driver and ghost reference pace at current lap
   * progress.
   *
   * @param liveLapProgress fraction of lap completed (0.0 to 1.0)
   * @param currentLapElapsed elapsed time in seconds on current lap
   * @param ghostLapTime reference ghost lap time in seconds
   * @return GhostGapResult with delta and projections
   */
  public static GhostGapResult calculateGhostGap(
      double liveLapProgress, double currentLapElapsed, double ghostLapTime) {
    if (ghostLapTime <= 0.0 || currentLapElapsed < 0.0) {
      return new GhostGapResult(0.0, 0.0, 0.0, Math.max(0.0, ghostLapTime));
    }

    double progress = Math.max(0.0, Math.min(1.0, liveLapProgress));
    if (progress <= 0.0001) {
      return new GhostGapResult(0.0, 0.0, 0.0, ghostLapTime);
    }

    double expectedGhostElapsed = progress * ghostLapTime;
    double deltaSeconds = expectedGhostElapsed - currentLapElapsed;
    double projectedLapTime = currentLapElapsed / progress;

    return new GhostGapResult(deltaSeconds, progress, projectedLapTime, ghostLapTime);
  }

  /**
   * Generates simulated ghost pace timeline with degradation curve and pit stop penalties.
   *
   * @param baseLaps base reference lap times
   * @param paceMultiplier pace scaling factor (e.g. 1.05 for 5% slower pace)
   * @param degradationPerLap time added per lap due to tire/fuel degradation
   * @param pitStopDuration time penalty added during pit stop lap
   * @param pitLap lap number at which pit stop occurs (1-based, <= 0 for no pit stop)
   * @return list of simulated laps
   */
  public static List<SimulatedLap> generateGhostLapSeries(
      List<Double> baseLaps,
      double paceMultiplier,
      double degradationPerLap,
      double pitStopDuration,
      int pitLap) {
    List<SimulatedLap> simulated = new ArrayList<>();
    if (baseLaps == null || baseLaps.isEmpty()) {
      return simulated;
    }

    double multiplier = paceMultiplier > 0.0 ? paceMultiplier : 1.0;
    double cumulativeTime = 0.0;

    for (int i = 0; i < baseLaps.size(); i++) {
      int lapNum = i + 1;
      double baseTime = baseLaps.get(i) != null ? Math.max(0.0, baseLaps.get(i)) : 0.0;
      double degradation = i * degradationPerLap;
      boolean isPit = lapNum == pitLap && pitStopDuration > 0.0;
      double pitPenalty = isPit ? pitStopDuration : 0.0;

      double finalLapTime = (baseTime * multiplier) + degradation + pitPenalty;
      cumulativeTime += finalLapTime;

      simulated.add(new SimulatedLap(lapNum, finalLapTime, cumulativeTime, isPit));
    }

    return simulated;
  }

  /**
   * Compares the lap-by-lap heat trajectories of two drivers.
   *
   * @param driverA first driver heat data
   * @param driverB second driver heat data
   * @return HeatTrajectoryComparison detailing deltas and lead changes
   */
  public static HeatTrajectoryComparison compareHeatTrajectories(
      DriverHeatData driverA, DriverHeatData driverB) {
    if (driverA == null || driverB == null) {
      return new HeatTrajectoryComparison(
          driverA != null ? driverA.getParticipantId() : null,
          driverB != null ? driverB.getParticipantId() : null,
          0,
          Collections.emptyList(),
          0,
          null,
          0.0,
          0.0,
          0.0);
    }

    int lapsA = driverA.getLapCount();
    int lapsB = driverB.getLapCount();
    int commonLaps = Math.min(lapsA, lapsB);

    if (commonLaps <= 0) {
      return new HeatTrajectoryComparison(
          driverA.getParticipantId(),
          driverB.getParticipantId(),
          0,
          Collections.emptyList(),
          0,
          null,
          0.0,
          0.0,
          0.0);
    }

    List<Double> deltas = new ArrayList<>();
    int leadChanges = 0;
    String previousLeader = null;
    double maxAdvantageA = 0.0;
    double maxAdvantageB = 0.0;

    for (int lap = 1; lap <= commonLaps; lap++) {
      double timeA = driverA.getTimeAtLap(lap);
      double timeB = driverB.getTimeAtLap(lap);
      // Delta: positive means A is faster/leading (less time), negative means B is leading
      double delta = timeB - timeA;
      deltas.add(delta);

      if (delta > 0) {
        maxAdvantageA = Math.max(maxAdvantageA, delta);
        if ("B".equals(previousLeader)) {
          leadChanges++;
        }
        previousLeader = "A";
      } else if (delta < 0) {
        double advantageB = Math.abs(delta);
        maxAdvantageB = Math.max(maxAdvantageB, advantageB);
        if ("A".equals(previousLeader)) {
          leadChanges++;
        }
        previousLeader = "B";
      }
    }

    double finalDelta = deltas.get(deltas.size() - 1);
    String leaderAtFinish =
        finalDelta > 0
            ? driverA.getParticipantId()
            : (finalDelta < 0 ? driverB.getParticipantId() : "TIE");

    return new HeatTrajectoryComparison(
        driverA.getParticipantId(),
        driverB.getParticipantId(),
        commonLaps,
        deltas,
        leadChanges,
        leaderAtFinish,
        finalDelta,
        maxAdvantageA,
        maxAdvantageB);
  }

  public static Map<String, Integer> calculateLapsLed(
      List<DriverHeatData> drivers, RankingMethod ranking, TiebreakerMethod tiebreaker) {
    Map<String, Integer> driverLapsLed = new HashMap<>();
    if (drivers == null || drivers.isEmpty()) {
      return driverLapsLed;
    }

    Map<String, DriverHeatData> driverMap = new HashMap<>();
    int maxLaps = 0;
    for (DriverHeatData dhd : drivers) {
      if (dhd == null || dhd.isEmptyParticipant()) continue;
      String driverId = dhd.getParticipantId();
      if (driverId == null || driverId.isEmpty()) continue;
      driverMap.put(driverId, dhd);
      driverLapsLed.put(driverId, 0);
      int lapCount = dhd.getLapCount();
      if (lapCount > maxLaps) {
        maxLaps = lapCount;
      }
    }

    if (maxLaps == 0 || driverMap.isEmpty()) {
      return driverLapsLed;
    }

    StandingsComparator comparator = new StandingsComparator(ranking, tiebreaker);

    for (int lapNum = 1; lapNum <= maxLaps; lapNum++) {
      List<StandingsParticipant> candidates = new ArrayList<>();
      for (Map.Entry<String, DriverHeatData> entry : driverMap.entrySet()) {
        String driverId = entry.getKey();
        DriverHeatData dhd = entry.getValue();
        if (dhd.getLapCount() >= lapNum
            && dhd.getLaps() != null
            && lapNum <= dhd.getLaps().size()) {
          double totalTime = dhd.getTimeAtLap(lapNum);
          double bestLap = Double.MAX_VALUE;
          List<Double> lapTimes = new ArrayList<>();
          for (int i = 0; i < lapNum; i++) {
            double t = dhd.getLaps().get(i).getLapTime();
            lapTimes.add(t);
            if (t > 0 && t < bestLap) {
              bestLap = t;
            }
          }
          if (bestLap == Double.MAX_VALUE) {
            bestLap = 0.0;
          }
          double avgLap = lapNum > 0 ? totalTime / lapNum : 0.0;
          double medLap = calculateMedian(lapTimes);

          candidates.add(
              new LapPerformanceSnapshot(
                  driverId,
                  lapNum,
                  totalTime,
                  bestLap,
                  avgLap,
                  medLap,
                  dhd.isEmptyParticipant(),
                  dhd.getSeed()));
        }
      }

      if (candidates.isEmpty()) continue;

      candidates.sort(comparator);

      String winner = candidates.get(0).getParticipantId();
      driverLapsLed.put(winner, driverLapsLed.getOrDefault(winner, 0) + 1);
    }

    return driverLapsLed;
  }

  private static double calculateMedian(List<Double> list) {
    if (list == null || list.isEmpty()) return 0.0;
    List<Double> sorted = new ArrayList<>(list);
    Collections.sort(sorted);
    int mid = sorted.size() / 2;
    if (sorted.size() % 2 == 1) return sorted.get(mid);
    return (sorted.get(mid - 1) + sorted.get(mid)) / 2.0;
  }
}
