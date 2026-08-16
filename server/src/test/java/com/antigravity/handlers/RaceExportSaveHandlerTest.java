package com.antigravity.handlers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.antigravity.context.DatabaseContext;
import com.antigravity.race.ClientSubscriptionManager;
import io.javalin.http.Context;
import java.io.File;
import java.nio.file.Path;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RaceExportSaveHandlerTest {

  private DatabaseContext databaseContext;
  private RaceExportSaveHandler handler;
  private Context ctx;

  @Before
  public void setUp() throws Exception {
    String tmpDir = System.getProperty("java.io.tmpdir");
    File tempFile = new File(tmpDir, "export_save_test_" + System.currentTimeMillis());
    tempFile.mkdirs();
    Path tempDir = tempFile.toPath();

    databaseContext = new DatabaseContext("testdb", null, tempDir.toString() + File.separator);
    ClientSubscriptionManager.setInstance(null);
    handler = new RaceExportSaveHandler(databaseContext);

    ctx = mock(Context.class);
    when(ctx.status(any(Integer.class))).thenReturn(ctx);
    when(ctx.contentType(any(String.class))).thenReturn(ctx);
    when(ctx.header(any(), any())).thenReturn(ctx);
    when(ctx.result(any(String.class))).thenReturn(ctx);
  }

  @After
  public void tearDown() {
    ClientSubscriptionManager.setInstance(null);
  }

  @Test
  public void testSaveRace_NoActiveRace_ShouldReturn404() {
    handler.saveRace(ctx);
    verify(ctx).status(404);
  }

  @Test
  public void testExportCsv_NoActiveRace_ShouldReturn404() {
    handler.exportRaceCsv(ctx);
    verify(ctx).status(404);
  }

  @Test
  public void testSaveRace_WhenRacing_ShouldReturn400() {
    com.antigravity.race.Race mockRace = mock(com.antigravity.race.Race.class);
    when(mockRace.getState()).thenReturn(new com.antigravity.race.states.Racing());
    ClientSubscriptionManager.getInstance().setRace(mockRace);

    handler.saveRace(ctx);
    verify(ctx).status(400);
  }

  @Test
  public void testGetSavedRaces_Success() {
    handler.getSavedRaces(ctx);
    verify(ctx).contentType("application/json");
  }

  @Test
  public void testDeleteSavedRace_NotFound() {
    when(ctx.pathParam("filename")).thenReturn("non_existent_race.json");
    handler.deleteSavedRace(ctx);
    verify(ctx).status(404);
  }

  @Test
  public void testLoadRace_NullFilename_Returns400() {
    java.util.HashMap<String, Object> body = new java.util.HashMap<>();
    when(ctx.bodyAsClass(java.util.HashMap.class)).thenReturn(body);

    handler.loadRace(ctx);
    verify(ctx).status(400);
  }

  @Test
  public void testLoadRace_NotFound_Returns404() {
    java.util.HashMap<String, Object> body = new java.util.HashMap<>();
    body.put("filename", "missing_save.json");
    when(ctx.bodyAsClass(java.util.HashMap.class)).thenReturn(body);

    handler.loadRace(ctx);
    verify(ctx).status(404);
  }

  @Test
  public void testExportLapDataAccessors() {
    RaceExportSaveHandler.ExportLapData lapData =
        new RaceExportSaveHandler.ExportLapData(
            "Driver A",
            "Actual Driver A",
            1,
            2,
            12.5,
            125.0,
            4.2,
            java.util.Arrays.asList(1.2, 1.5, 1.5));

    org.junit.Assert.assertEquals("Driver A", lapData.getDriverName());
    org.junit.Assert.assertEquals("Actual Driver A", lapData.getActualDriverName());
    org.junit.Assert.assertEquals(1, lapData.getHeatNumber());
    org.junit.Assert.assertEquals(2, lapData.getLaneNumber());
    org.junit.Assert.assertEquals(12.5, lapData.getAbsoluteHeatLapTime(), 0.001);
    org.junit.Assert.assertEquals(125.0, lapData.getAbsoluteLapTime(), 0.001);
    org.junit.Assert.assertEquals(4.2, lapData.getLapTime(), 0.001);
    org.junit.Assert.assertEquals(3, lapData.getSegments().size());

    RaceExportSaveHandler.ExportLapData nullSegments =
        new RaceExportSaveHandler.ExportLapData("Driver B", "Actual B", 2, 1, 5.0, 50.0, 5.0, null);
    org.junit.Assert.assertNotNull(nullSegments.getSegments());
    org.junit.Assert.assertTrue(nullSegments.getSegments().isEmpty());
  }

  @Test
  public void testExportCsv_WithActiveRace() {
    com.antigravity.models.Driver d1 =
        new com.antigravity.models.Driver("Alice", "Ally", "d1", "1");
    com.antigravity.race.RaceParticipant p1 = new com.antigravity.race.RaceParticipant(d1);

    com.antigravity.models.Lane lane = new com.antigravity.models.Lane("red", "black", 100);
    com.antigravity.models.Track track =
        new com.antigravity.models.Track.Builder()
            .name("Track 1")
            .lanes(java.util.Collections.singletonList(lane))
            .build();

    com.antigravity.models.Race model =
        new com.antigravity.models.Race.Builder()
            .withName("Active Race")
            .withEntityId("r1")
            .build();

    com.antigravity.race.Race activeRace =
        new com.antigravity.race.Race.Builder()
            .model(model)
            .drivers(java.util.Collections.singletonList(p1))
            .track(track)
            .isDemoMode(true)
            .build();

    ClientSubscriptionManager.getInstance().setRace(activeRace);

    handler.exportRaceCsv(ctx);
    verify(ctx).contentType("text/csv");
  }

  @Test
  public void testSaveAndLoadRace_RoundTrip() {
    com.antigravity.models.Driver d1 =
        new com.antigravity.models.Driver("Alice", "Ally", "d1", "1");
    com.antigravity.race.RaceParticipant p1 = new com.antigravity.race.RaceParticipant(d1);

    com.antigravity.models.Lane lane = new com.antigravity.models.Lane("red", "black", 100);
    com.antigravity.models.Track track =
        new com.antigravity.models.Track.Builder()
            .name("Track 1")
            .lanes(java.util.Collections.singletonList(lane))
            .build();

    com.antigravity.models.Race model =
        new com.antigravity.models.Race.Builder()
            .withName("Saved Race")
            .withEntityId("r_saved")
            .build();

    com.antigravity.race.Race activeRace =
        new com.antigravity.race.Race.Builder()
            .model(model)
            .drivers(java.util.Collections.singletonList(p1))
            .track(track)
            .isDemoMode(true)
            .build();

    ClientSubscriptionManager.getInstance().setRace(activeRace);

    handler.saveRace(ctx);
    verify(ctx).status(200);

    // Now get saved races
    io.javalin.http.Context getCtx = mock(io.javalin.http.Context.class);
    when(getCtx.contentType(any(String.class))).thenReturn(getCtx);
    handler.getSavedRaces(getCtx);
    verify(getCtx).contentType("application/json");
  }
}
