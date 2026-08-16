package com.antigravity.handlers;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.antigravity.service.ServerConfigService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.lang.reflect.Method;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

public class SettingsTaskHandlerTest {

  private Javalin app;
  private SettingsTaskHandler handler;
  private Context ctx;
  private ServerConfigService configService;

  @Before
  public void setUp() {

    app = mock(Javalin.class);
    ctx = mock(Context.class);
    configService = mock(ServerConfigService.class);
    handler = org.mockito.Mockito.spy(new SettingsTaskHandler(app, configService));

    // Use doNothing for our wrapper methods to avoid calling real ctx methods
    org.mockito.Mockito.doNothing().when(handler).setStatus(any(), anyInt());
    org.mockito.Mockito.doNothing().when(handler).setResult(any(), anyString());
    org.mockito.Mockito.doReturn(null).when(handler).getQueryParam(any(), anyString());
  }

  @Test
  public void testSetLogLevel_Success() throws Exception {
    org.mockito.Mockito.doReturn("debug").when(handler).getQueryParam(ctx, "level");

    // Get access to the private method
    Method setLogLevelMethod =
        SettingsTaskHandler.class.getDeclaredMethod("setLogLevel", Context.class);
    setLogLevelMethod.setAccessible(true);
    setLogLevelMethod.invoke(handler, ctx);

    // Verify Logback level was updated
    LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
    Logger logger = loggerContext.getLogger("com.antigravity");
    assertEquals(Level.DEBUG, logger.getLevel());

    verify(handler).setStatus(ctx, 200);
    verify(handler).setResult(ctx, "Server log level updated to debug");
  }

  @Test
  public void testSetLogLevel_MissingParameter() throws Exception {
    org.mockito.Mockito.doReturn(null).when(handler).getQueryParam(ctx, "level");

    Method setLogLevelMethod =
        SettingsTaskHandler.class.getDeclaredMethod("setLogLevel", Context.class);
    setLogLevelMethod.setAccessible(true);
    setLogLevelMethod.invoke(handler, ctx);

    verify(handler).setStatus(ctx, 400);
    verify(handler).setResult(ctx, "Level parameter is required");
  }

  @Test
  public void testSetLogLevel_InvalidLevel() throws Exception {
    org.mockito.Mockito.doReturn("error").when(handler).getQueryParam(ctx, "level");

    Method setLogLevelMethod =
        SettingsTaskHandler.class.getDeclaredMethod("setLogLevel", Context.class);
    setLogLevelMethod.setAccessible(true);
    setLogLevelMethod.invoke(handler, ctx);

    LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
    Logger logger = loggerContext.getLogger("com.antigravity");
    assertEquals(Level.ERROR, logger.getLevel());

    verify(handler).setStatus(ctx, 200);
    verify(handler).setResult(ctx, "Server log level updated to error");
  }

  @Test
  public void testSetDirectorPassword_Success() throws Exception {
    Method setDirectorPasswordMethod =
        SettingsTaskHandler.class.getDeclaredMethod("setDirectorPassword", Context.class);
    setDirectorPasswordMethod.setAccessible(true);

    org.mockito.Mockito.doReturn(
            new Object() {
              public String password = "new_secret_pwd";
            })
        .when(ctx)
        .bodyAsClass(any());

    // Call using reflection with simulated body
    SettingsTaskHandler.class.getDeclaredClasses(); // Load inner class PasswordRequest
    Class<?> pwReqClass = null;
    for (Class<?> c : SettingsTaskHandler.class.getDeclaredClasses()) {
      if (c.getSimpleName().equals("PasswordRequest")) {
        pwReqClass = c;
        break;
      }
    }
    java.lang.reflect.Constructor<?> ctor = pwReqClass.getDeclaredConstructor();
    ctor.setAccessible(true);
    Object reqInst = ctor.newInstance();
    pwReqClass.getField("password").set(reqInst, "new_secret_pwd");
    org.mockito.Mockito.doReturn(reqInst).when(ctx).bodyAsClass(any());

    setDirectorPasswordMethod.invoke(handler, ctx);
    verify(configService).setDirectorPassword("new_secret_pwd");
    verify(handler).setStatus(ctx, 200);
    verify(handler).setResult(ctx, "Director password updated");
  }

  @Test
  public void testGetAuthSettings_Success() throws Exception {
    Method getAuthSettingsMethod =
        SettingsTaskHandler.class.getDeclaredMethod("getAuthSettings", Context.class);
    getAuthSettingsMethod.setAccessible(true);

    org.mockito.Mockito.when(configService.getDirectorPassword()).thenReturn("pass123");
    getAuthSettingsMethod.invoke(handler, ctx);
    verify(ctx).json(any());
  }
}
