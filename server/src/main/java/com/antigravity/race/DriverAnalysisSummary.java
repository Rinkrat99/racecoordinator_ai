package com.antigravity.race;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class DriverAnalysisSummary {

  public static class LaneStats {
    private String laneName;
    private int laneNumber;
    private double totalLaps;
    private double totalTime;
    private double averageLapTime;
    private double medianLapTime;
    private double bestLapTime;
    private double standardDeviation;
    private double consistencyScore;
    private double averageTop5;
    private double averageTop10;
    private double averageTop15;
    private double top2Consecutive;
    private double top3Consecutive;

    public LaneStats() {}

    public LaneStats(
        String laneName,
        int laneNumber,
        double totalLaps,
        double totalTime,
        double averageLapTime,
        double medianLapTime,
        double bestLapTime,
        double standardDeviation,
        double consistencyScore,
        double averageTop5,
        double averageTop10,
        double averageTop15,
        double top2Consecutive,
        double top3Consecutive) {
      this.laneName = laneName;
      this.laneNumber = laneNumber;
      this.totalLaps = totalLaps;
      this.totalTime = totalTime;
      this.averageLapTime = averageLapTime;
      this.medianLapTime = medianLapTime;
      this.bestLapTime = bestLapTime;
      this.standardDeviation = standardDeviation;
      this.consistencyScore = consistencyScore;
      this.averageTop5 = averageTop5;
      this.averageTop10 = averageTop10;
      this.averageTop15 = averageTop15;
      this.top2Consecutive = top2Consecutive;
      this.top3Consecutive = top3Consecutive;
    }

    @JsonProperty("laneName")
    public String getLaneName() {
      return laneName;
    }

    public void setLaneName(String laneName) {
      this.laneName = laneName;
    }

    @JsonProperty("laneNumber")
    public int getLaneNumber() {
      return laneNumber;
    }

    public void setLaneNumber(int laneNumber) {
      this.laneNumber = laneNumber;
    }

    @JsonProperty("totalLaps")
    public double getTotalLaps() {
      return totalLaps;
    }

    public void setTotalLaps(double totalLaps) {
      this.totalLaps = totalLaps;
    }

    @JsonProperty("totalTime")
    public double getTotalTime() {
      return totalTime;
    }

    public void setTotalTime(double totalTime) {
      this.totalTime = totalTime;
    }

    @JsonProperty("averageLapTime")
    public double getAverageLapTime() {
      return averageLapTime;
    }

    public void setAverageLapTime(double averageLapTime) {
      this.averageLapTime = averageLapTime;
    }

    @JsonProperty("medianLapTime")
    public double getMedianLapTime() {
      return medianLapTime;
    }

    public void setMedianLapTime(double medianLapTime) {
      this.medianLapTime = medianLapTime;
    }

    @JsonProperty("bestLapTime")
    public double getBestLapTime() {
      return bestLapTime;
    }

    public void setBestLapTime(double bestLapTime) {
      this.bestLapTime = bestLapTime;
    }

    @JsonProperty("standardDeviation")
    public double getStandardDeviation() {
      return standardDeviation;
    }

    public void setStandardDeviation(double standardDeviation) {
      this.standardDeviation = standardDeviation;
    }

    @JsonProperty("consistencyScore")
    public double getConsistencyScore() {
      return consistencyScore;
    }

    public void setConsistencyScore(double consistencyScore) {
      this.consistencyScore = consistencyScore;
    }

    @JsonProperty("averageTop5")
    public double getAverageTop5() {
      return averageTop5;
    }

    public void setAverageTop5(double averageTop5) {
      this.averageTop5 = averageTop5;
    }

    @JsonProperty("averageTop10")
    public double getAverageTop10() {
      return averageTop10;
    }

    public void setAverageTop10(double averageTop10) {
      this.averageTop10 = averageTop10;
    }

    @JsonProperty("averageTop15")
    public double getAverageTop15() {
      return averageTop15;
    }

    public void setAverageTop15(double averageTop15) {
      this.averageTop15 = averageTop15;
    }

    @JsonProperty("top2Consecutive")
    public double getTop2Consecutive() {
      return top2Consecutive;
    }

    public void setTop2Consecutive(double top2Consecutive) {
      this.top2Consecutive = top2Consecutive;
    }

    @JsonProperty("top3Consecutive")
    public double getTop3Consecutive() {
      return top3Consecutive;
    }

    public void setTop3Consecutive(double top3Consecutive) {
      this.top3Consecutive = top3Consecutive;
    }
  }

  private String driverName;
  private String driverId;
  private List<LaneStats> laneStats = new ArrayList<>();

  public DriverAnalysisSummary() {}

  public DriverAnalysisSummary(String driverName, String driverId) {
    this.driverName = driverName;
    this.driverId = driverId;
  }

  @JsonProperty("driverName")
  public String getDriverName() {
    return driverName;
  }

  public void setDriverName(String driverName) {
    this.driverName = driverName;
  }

  @JsonProperty("driverId")
  public String getDriverId() {
    return driverId;
  }

  public void setDriverId(String driverId) {
    this.driverId = driverId;
  }

  @JsonProperty("laneStats")
  public List<LaneStats> getLaneStats() {
    return laneStats;
  }

  public void setLaneStats(List<LaneStats> laneStats) {
    this.laneStats = laneStats != null ? laneStats : new ArrayList<>();
  }

  public void addLaneStats(LaneStats stats) {
    this.laneStats.add(stats);
  }
}
