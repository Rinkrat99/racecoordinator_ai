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
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DriverLaneHeatHandlerTest {

  private DatabaseContext databaseContext;
  private DriverLaneHeatHandler handler;
  private Context ctx;

  @Before
  public void setUp() throws Exception {
    String tmpDir = System.getProperty("java.io.tmpdir");
    File tempFile = new File(tmpDir, "driver_lane_heat_test_" + System.currentTimeMillis());
    tempFile.mkdirs();
    Path tempDir = tempFile.toPath();

    databaseContext = new DatabaseContext("testdb", null, tempDir.toString() + File.separator);
    ClientSubscriptionManager.setInstance(null);
    handler = new DriverLaneHeatHandler(databaseContext);

    ctx = mock(Context.class);
    when(ctx.status(any(Integer.class))).thenReturn(ctx);
    when(ctx.result(any(String.class))).thenReturn(ctx);
  }

  @After
  public void tearDown() {
    ClientSubscriptionManager.setInstance(null);
  }

  @Test
  public void testUpdateUserLaps_NoActiveRace_ShouldReturn404() {
    Map<String, String> pathParams = new HashMap<>();
    pathParams.put("lane", "0");
    Map<String, Object> body = new HashMap<>();
    body.put("userLaps", 5.0);

    handler.updateUserLaps(ctx, pathParams, body);
    verify(ctx).status(404);
  }

  @Test
  public void testChangeLane_NoActiveRace_ShouldReturn404() {
    when(ctx.pathParam("fromLane")).thenReturn("0");
    when(ctx.pathParam("toLane")).thenReturn("1");

    handler.changeLane(ctx);
    verify(ctx).status(404);
  }

  @Test
  public void testResetLaneHeatData_NoActiveRace_ShouldReturn404() {
    when(ctx.pathParam("lane")).thenReturn("all");

    handler.resetLaneHeatData(ctx);
    verify(ctx).status(404);
  }

  @Test
  public void testChangeActualDriver_NoActiveRace_ShouldReturn404() {
    when(ctx.pathParam("lane")).thenReturn("0");
    HashMap<String, String> body = new HashMap<>();
    body.put("driverId", "d1");
    when(ctx.bodyAsClass(HashMap.class)).thenReturn(body);

    handler.changeActualDriver(ctx);
    verify(ctx).status(404);
  }

  @Test
  public void testChangeHeatActualDriver_NoActiveRace_ShouldReturn404() {
    when(ctx.pathParam("heatNumber")).thenReturn("1");
    when(ctx.pathParam("lane")).thenReturn("0");
    HashMap<String, String> body = new HashMap<>();
    body.put("driverId", "d1");
    when(ctx.bodyAsClass(HashMap.class)).thenReturn(body);

    handler.changeHeatActualDriver(ctx);
    verify(ctx).status(404);
  }

  @Test
  public void testUpdateHeatUserLaps_NoActiveRace_ShouldReturn404() {
    when(ctx.pathParam("heatNumber")).thenReturn("1");
    when(ctx.pathParam("lane")).thenReturn("0");
    HashMap<String, Object> body = new HashMap<>();
    body.put("userLaps", 2.5);
    when(ctx.bodyAsClass(HashMap.class)).thenReturn(body);

    handler.updateHeatUserLaps(ctx);
    verify(ctx).status(404);
  }

  @Test
  public void testUpdateBatchUserLaps_NoActiveRace_ShouldReturn404() {
    java.util.ArrayList<Map<String, Object>> updates = new java.util.ArrayList<>();
    when(ctx.bodyAsClass(java.util.List.class)).thenReturn(updates);

    handler.updateBatchUserLaps(ctx);
    verify(ctx).status(404);
  }
}
