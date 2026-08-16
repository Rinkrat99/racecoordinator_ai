package com.antigravity.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.antigravity.context.DatabaseContext;
import com.antigravity.context.RaceScope;
import com.antigravity.models.Driver;
import com.antigravity.models.DriverStatistics;
import com.antigravity.models.DriverTrackStats;
import com.antigravity.models.GlobalStatistics;
import com.antigravity.models.Lane;
import com.antigravity.models.Race;
import com.antigravity.models.Track;
import com.antigravity.race.DriverHeatData;
import com.antigravity.race.RaceParticipant;
import com.antigravity.race.states.Racing;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class DatabaseStatisticsServiceTest {
  @Rule public TemporaryFolder tempFolder = new TemporaryFolder();

  private DatabaseContext context;
  private DatabaseStatisticsService statsService;

  @Before
  public void setUp() throws Exception {
    String rootDir = tempFolder.newFolder("db_root").getAbsolutePath() + File.separator;
    context = new DatabaseContext("test_db", null, rootDir);
    statsService = new DatabaseStatisticsService();
  }

  @Test
  public void testGetGlobalStatistics_DefaultWhenNullOrNotFound() {
    GlobalStatistics nullStats =
        statsService.getGlobalStatistics(context, (String) null, RaceScope.PRODUCTION);
    assertNotNull(nullStats);

    GlobalStatistics emptyStats =
        statsService.getGlobalStatistics(context, "non_existent_race", false);
    assertNotNull(emptyStats);
    assertEquals("non_existent_race", emptyStats.getRaceEntityId());
  }

  @Test
  public void testGetDriverStatistics_EmptyDefault() {
    DriverStatistics stats = statsService.getDriverStatistics(context, "driver_1", "race_1", false);
    assertNotNull(stats);
    assertEquals("driver_1", stats.getDriverId());
    assertEquals("race_1", stats.getRaceId());
    assertEquals(0.0, stats.getBestLapTime(), 0.001);

    assertNull(statsService.getDriverStatistics(null, "d1", "r1", false));
    assertNull(statsService.getDriverStatistics(context, null, "r1", false));
    assertNull(statsService.getDriverStatistics(context, "", "r1", false));
  }

  @Test
  public void testSaveAndGetDriverTrackStats() {
    DriverTrackStats stats = new DriverTrackStats();
    stats.setId("driver_1_track_1");
    stats.setDriverId("driver_1");
    stats.setTrackId("track_1");
    stats.setTotalRaces(5);

    statsService.saveDriverTrackStats(context, stats, false);

    DriverTrackStats fetched =
        statsService.getDriverTrackStats(context, "driver_1", "track_1", false);
    assertNotNull(fetched);
    assertEquals("driver_1", fetched.getDriverId());
    assertEquals("track_1", fetched.getTrackId());
    assertEquals(5, fetched.getTotalRaces());

    assertNull(statsService.getDriverTrackStats(null, "d1", "t1", false));
    assertNull(statsService.getDriverTrackStats(context, null, "t1", false));
    assertNull(statsService.getDriverTrackStats(context, "d1", null, false));
  }

  @Test
  public void testUpdateGlobalStatisticsAndDriverStatisticsFromRace() {
    Driver driver1 = new Driver("Alice", "Ally", "d1", "1");
    Driver driver2 = new Driver("Bob", "Bobby", "d2", "2");

    RaceParticipant rp1 = new RaceParticipant(driver1);
    RaceParticipant rp2 = new RaceParticipant(driver2);

    List<Lane> lanes = Arrays.asList(new Lane("red", "black", 100), new Lane("blue", "white", 100));
    Track track =
        new Track.Builder()
            .name("Grand Prix Track")
            .lanes(lanes)
            .entityId("track_gp")
            .id("1")
            .build();

    Race raceModel =
        new Race.Builder()
            .withName("Championship Round 1")
            .withTrackEntityId("track_gp")
            .withEntityId("race_championship_1")
            .build();

    com.antigravity.race.Race runtimeRace =
        new com.antigravity.race.Race.Builder()
            .model(raceModel)
            .drivers(Arrays.asList(rp1, rp2))
            .track(track)
            .isDemoMode(true)
            .build();

    runtimeRace.changeState(new Racing());
    DriverHeatData dhd1 = runtimeRace.getCurrentHeat().getDrivers().get(0);
    DriverHeatData dhd2 = runtimeRace.getCurrentHeat().getDrivers().get(1);

    dhd1.addLap(3.45, false, true);
    dhd1.addLap(3.40, false, true);
    dhd2.addLap(3.60, false, true);

    runtimeRace.getRecordsManager().onLap(dhd1, 3.40, 0);
    runtimeRace.getRecordsManager().onLap(dhd2, 3.60, 1);

    // Update Global Statistics
    statsService.updateGlobalStatistics(context, runtimeRace);

    GlobalStatistics globalStats =
        statsService.getGlobalStatistics(context, "race_championship_1", true);
    assertNotNull(globalStats);
    assertEquals("race_championship_1", globalStats.getRaceEntityId());
    assertTrue(globalStats.getTotalRaces() >= 1);

    // Save Driver Statistics
    statsService.saveDriverStatistics(context, runtimeRace);
    DriverStatistics d1Stats =
        statsService.getDriverStatistics(context, "d1", "race_championship_1", true);
    assertNotNull(d1Stats);

    // Update Driver Track Stats
    statsService.updateDriverTrackStats(context, runtimeRace, true);
    DriverTrackStats dtStats = statsService.getDriverTrackStats(context, "d1", "track_gp", true);
    assertNotNull(dtStats);
    assertTrue(dtStats.getTotalRaces() >= 1);
  }
}
