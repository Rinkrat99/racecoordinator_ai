package com.antigravity.service;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import com.antigravity.protocols.interfaces.LogReaderSerialConnection;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LogReplayServiceTest {

  private File tempLogFile;
  private LogReplayService replayService;

  @Before
  public void setUp() throws IOException {
    com.antigravity.race.ClientSubscriptionManager.setInstance(null);
    tempLogFile = File.createTempFile("test_log", ".txt");
  }

  @After
  public void tearDown() {
    LogReplayService.reset();
    com.antigravity.race.ClientSubscriptionManager.setInstance(null);
    if (tempLogFile != null && tempLogFile.exists()) {
      tempLogFile.delete();
    }
  }

  @Test
  public void testLogReplayParsesSerialBytes() throws Exception {
    try (FileWriter writer = new FileWriter(tempLogFile)) {
      writer.write(
          "2026-07-18 12:12:00.001 [Thread-1] DEBUG com.antigravity.protocols.interfaces.SerialConnection - [COM3] Received: 41 34 0A\n");
      writer.write(
          "2026-07-18 12:12:00.005 [Thread-1] DEBUG com.antigravity.protocols.interfaces.SerialConnection - [COM3] Received: 42 35\n");
    }

    LogReaderSerialConnection mockConnection = new LogReaderSerialConnection();
    mockConnection.connect("COM3", 9600, false);

    List<byte[]> receivedDataList = new ArrayList<>();
    CountDownLatch latch = new CountDownLatch(2);

    mockConnection.addListener(
        new SerialPortDataListener() {
          @Override
          public int getListeningEvents() {
            return com.fazecast.jSerialComm.SerialPort.LISTENING_EVENT_DATA_RECEIVED;
          }

          @Override
          public void serialEvent(SerialPortEvent event) {
            receivedDataList.add(event.getReceivedData());
            latch.countDown();
          }
        });

    LogReplayService.reset();

    // Use reflection to instantiate and register BEFORE starting the thread
    // This avoids race conditions on slow CI runners
    java.lang.reflect.Constructor<LogReplayService> constructor =
        LogReplayService.class.getDeclaredConstructor(String.class);
    constructor.setAccessible(true);
    replayService = constructor.newInstance(tempLogFile.getAbsolutePath());

    java.lang.reflect.Field instanceField = LogReplayService.class.getDeclaredField("instance");
    instanceField.setAccessible(true);
    instanceField.set(null, replayService);

    replayService.registerSerialConnection(mockConnection);
    replayService.start();

    boolean completed = latch.await(2, TimeUnit.SECONDS);
    assertTrue("Did not receive expected serial events in time", completed);

    assertArrayEquals(new byte[] {0x41, 0x34, 0x0A}, receivedDataList.get(0));
    assertArrayEquals(new byte[] {0x42, 0x35}, receivedDataList.get(1));
  }

  @Test
  public void testLogReplayStatusTracking() throws Exception {
    try (FileWriter writer = new FileWriter(tempLogFile)) {
      writer.write(
          "2026-07-18 12:12:00.001 [Thread-1] DEBUG com.antigravity.protocols.interfaces.SerialConnection - [COM3] Received: 41 34 0A\n");
      writer.write(
          "2026-07-18 12:12:00.005 [Thread-1] DEBUG com.antigravity.protocols.interfaces.SerialConnection - [COM3] Received: 42 35\n");
    }

    LogReplayService.reset();

    java.lang.reflect.Constructor<LogReplayService> constructor =
        LogReplayService.class.getDeclaredConstructor(String.class);
    constructor.setAccessible(true);
    replayService = constructor.newInstance(tempLogFile.getAbsolutePath());

    java.lang.reflect.Field instanceField = LogReplayService.class.getDeclaredField("instance");
    instanceField.setAccessible(true);
    instanceField.set(null, replayService);

    replayService.start();

    // Wait briefly for replay to finish
    Thread.sleep(500);

    com.antigravity.proto.LogReplayStatus status = replayService.getLogReplayStatus();
    assertTrue("Log should be finished", status.getIsFinished());
    org.junit.Assert.assertEquals("Should have processed 2 lines", 2, status.getLinesProcessed());
    org.junit.Assert.assertEquals("Total lines should be 2", 2, status.getTotalLines());
  }

  @Test
  public void testPreRaceIdleTimeIsFastForwarded() throws Exception {
    try (FileWriter writer = new FileWriter(tempLogFile)) {
      writer.write(
          "2026-07-18 12:12:00.000 [Thread-1] INFO com.antigravity.App - Starting server\n");
      writer.write(
          "2026-07-18 12:12:30.000 [Thread-1] INFO com.antigravity.App - Setup page idle\n");
      writer.write("2026-07-18 12:13:00.000 [Thread-1] INFO com.antigravity.App - Still idle\n");
    }

    LogReplayService.reset();

    java.lang.reflect.Constructor<LogReplayService> constructor =
        LogReplayService.class.getDeclaredConstructor(String.class);
    constructor.setAccessible(true);
    replayService = constructor.newInstance(tempLogFile.getAbsolutePath());

    java.lang.reflect.Field instanceField = LogReplayService.class.getDeclaredField("instance");
    instanceField.setAccessible(true);
    instanceField.set(null, replayService);

    long startMs = System.currentTimeMillis();
    replayService.start();

    // Wait briefly for replay to finish - with 60 seconds of log gaps, fast forwarding should take
    // < 2000ms
    long deadline = System.currentTimeMillis() + 2000;
    while (!replayService.getLogReplayStatus().getIsFinished()
        && System.currentTimeMillis() < deadline) {
      Thread.sleep(50);
    }
    long elapsed = System.currentTimeMillis() - startMs;

    com.antigravity.proto.LogReplayStatus status = replayService.getLogReplayStatus();
    assertTrue("Log should be finished", status.getIsFinished());
    org.junit.Assert.assertEquals("Should have processed 3 lines", 3, status.getLinesProcessed());
    assertTrue("Pre-race idle time (60s total) should finish in < 2000ms", elapsed < 2000);
  }

  @Test
  public void testProcessLogMessage_RaceConfigDumpAndCommands() throws Exception {
    LogReplayService.reset();

    java.lang.reflect.Constructor<LogReplayService> constructor =
        LogReplayService.class.getDeclaredConstructor(String.class);
    constructor.setAccessible(true);
    replayService = constructor.newInstance(tempLogFile.getAbsolutePath());

    java.lang.reflect.Method processMethod =
        LogReplayService.class.getDeclaredMethod("processLogMessage", String.class);
    processMethod.setAccessible(true);

    // 1. RaceConfigDump
    com.antigravity.models.HeatScoring heatScoring =
        new com.antigravity.models.HeatScoring(
            com.antigravity.models.HeatScoring.FinishMethod.Timed,
            120,
            com.antigravity.models.HeatScoring.HeatRanking.LAP_COUNT,
            com.antigravity.models.HeatScoring.HeatRankingTiebreaker.FASTEST_LAP_TIME);
    com.antigravity.models.Race race =
        new com.antigravity.models.Race.Builder()
            .withName("Replay Race")
            .withTrackEntityId("t_rep")
            .withHeatRotationType(com.antigravity.models.HeatRotationType.RoundRobin)
            .withHeatScoring(heatScoring)
            .withOverallScoring(new com.antigravity.models.OverallScoring())
            .withEntityId("r_rep")
            .build();
    com.antigravity.models.Track track =
        new com.antigravity.models.Track.Builder()
            .name("Replay Track")
            .lanes(java.util.Arrays.asList(new com.antigravity.models.Lane("red", "black", 50)))
            .entityId("t_rep")
            .build();
    com.antigravity.models.Driver driver =
        new com.antigravity.models.Driver("Driver 1", "D1", "d1", null);
    com.antigravity.race.RaceParticipant participant =
        new com.antigravity.race.RaceParticipant(driver);

    com.antigravity.race.Race activeRace =
        new com.antigravity.race.Race.Builder()
            .model(race)
            .track(track)
            .drivers(java.util.Arrays.asList(participant))
            .isDemoMode(true)
            .build();
    com.antigravity.race.ClientSubscriptionManager.getInstance().setRace(activeRace);
    org.junit.Assert.assertNotNull(
        com.antigravity.race.ClientSubscriptionManager.getInstance().getRace());

    // 2. ReplayCommandDump - startRace
    processMethod.invoke(replayService, "ReplayCommandDump: {\"command\":\"startRace\"}");

    // 3. ReplayCommandDump - pauseRace
    processMethod.invoke(replayService, "ReplayCommandDump: {\"command\":\"pauseRace\"}");

    // 4. ReplayCommandDump - setMainPower
    processMethod.invoke(
        replayService,
        "ReplayCommandDump: {\"command\":\"setMainPower\",\"parameters\":{\"on\":true}}");

    // 5. ReplayCommandDump - setLanePower
    processMethod.invoke(
        replayService,
        "ReplayCommandDump: {\"command\":\"setLanePower\",\"parameters\":{\"lane\":1,\"on\":true}}");

    // 6. ReplayCommandDump - endRace
    processMethod.invoke(replayService, "ReplayCommandDump: {\"command\":\"endRace\"}");

    // 7. Register & unregister connection
    com.antigravity.protocols.interfaces.LogReaderSerialConnection conn =
        new com.antigravity.protocols.interfaces.LogReaderSerialConnection();
    replayService.registerSerialConnection(conn);
    replayService.unregisterSerialConnection(conn);
  }
}
