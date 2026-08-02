package com.antigravity.context;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.antigravity.util.RequestContextUtils;
import io.javalin.http.Context;
import org.junit.Test;

public class RaceScopeTest {

  @Test
  public void testRaceScopeCollectionNames() {
    assertEquals("race_history", RaceScope.PRODUCTION.getCollectionName("race_history"));
    assertEquals("demo_race_history", RaceScope.DEMO.getCollectionName("race_history"));
    assertEquals("driver_statistics", RaceScope.PRODUCTION.getCollectionName("driver_statistics"));
    assertEquals("demo_driver_statistics", RaceScope.DEMO.getCollectionName("driver_statistics"));
    assertNull(RaceScope.PRODUCTION.getCollectionName(null));
  }

  @Test
  public void testRaceScopeFromBoolean() {
    assertEquals(RaceScope.DEMO, RaceScope.fromBoolean(true));
    assertEquals(RaceScope.PRODUCTION, RaceScope.fromBoolean(false));
    assertTrue(RaceScope.DEMO.isDemo());
    assertFalse(RaceScope.PRODUCTION.isDemo());
  }

  @Test
  public void testRequestContextUtilsWithQueryParamDemo() {
    Context ctx = mock(Context.class);
    when(ctx.queryParam("demo")).thenReturn("true");
    assertEquals(RaceScope.DEMO, RequestContextUtils.getRaceScope(ctx));
    assertTrue(RequestContextUtils.isDemoMode(ctx));
  }

  @Test
  public void testRequestContextUtilsWithQueryParamIsDemo() {
    Context ctx = mock(Context.class);
    when(ctx.queryParam("isDemo")).thenReturn("true");
    assertEquals(RaceScope.DEMO, RequestContextUtils.getRaceScope(ctx));
    assertTrue(RequestContextUtils.isDemoMode(ctx));
  }

  @Test
  public void testRequestContextUtilsWithHeader() {
    Context ctx = mock(Context.class);
    when(ctx.header("X-Race-Demo-Mode")).thenReturn("true");
    assertEquals(RaceScope.DEMO, RequestContextUtils.getRaceScope(ctx));
    assertTrue(RequestContextUtils.isDemoMode(ctx));
  }

  @Test
  public void testRequestContextUtilsWithJsonBody() {
    Context ctx = mock(Context.class);
    when(ctx.body()).thenReturn("{\"isDemo\": true, \"otherField\": \"test\"}");
    assertEquals(RaceScope.DEMO, RequestContextUtils.getRaceScope(ctx));
    assertTrue(RequestContextUtils.isDemoMode(ctx));
  }

  @Test
  public void testRequestContextUtilsDefaultsToProduction() {
    Context ctx = mock(Context.class);
    when(ctx.queryParam("demo")).thenReturn(null);
    when(ctx.queryParam("isDemo")).thenReturn("false");
    assertEquals(RaceScope.PRODUCTION, RequestContextUtils.getRaceScope(ctx));
    assertFalse(RequestContextUtils.isDemoMode(ctx));
  }
}
