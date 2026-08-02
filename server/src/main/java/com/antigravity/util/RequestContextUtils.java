package com.antigravity.util;

import com.antigravity.context.RaceScope;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.Context;

/**
 * Utility for extract request-scoped context (such as RaceScope / Demo Mode) consistently from
 * Javalin HTTP contexts across all API endpoints.
 */
public final class RequestContextUtils {
  private static final ObjectMapper mapper = new ObjectMapper();

  private RequestContextUtils() {}

  /**
   * Evaluates whether an HTTP request target is in demo mode by checking: 1. Query param "demo" or
   * "isDemo" 2. Header "X-Race-Demo-Mode" or "X-Demo-Mode" 3. Request JSON Body field "isDemo" or
   * "demo"
   *
   * @param ctx The Javalin request context.
   * @return The resolved RaceScope (DEMO or PRODUCTION).
   */
  public static RaceScope getRaceScope(Context ctx) {
    if (ctx == null) {
      return RaceScope.PRODUCTION;
    }

    // 1. Check Query Parameters
    String demoQuery = ctx.queryParam("demo");
    String isDemoQuery = ctx.queryParam("isDemo");
    if ("true".equalsIgnoreCase(demoQuery) || "true".equalsIgnoreCase(isDemoQuery)) {
      return RaceScope.DEMO;
    }

    // 2. Check Headers
    String headerDemo = ctx.header("X-Race-Demo-Mode");
    String headerAlt = ctx.header("X-Demo-Mode");
    if ("true".equalsIgnoreCase(headerDemo) || "true".equalsIgnoreCase(headerAlt)) {
      return RaceScope.DEMO;
    }

    // 3. Check JSON Body if present
    try {
      String body = ctx.body();
      if (body != null && !body.trim().isEmpty() && body.trim().startsWith("{")) {
        JsonNode root = mapper.readTree(body);
        if (root.has("isDemo") && root.get("isDemo").asBoolean(false)) {
          return RaceScope.DEMO;
        }
        if (root.has("demo") && root.get("demo").asBoolean(false)) {
          return RaceScope.DEMO;
        }
      }
    } catch (Exception ignored) {
      // Body may not be JSON or may already be consumed/unreadable; ignore
    }

    return RaceScope.PRODUCTION;
  }

  /** Helper method returning true if the request context resolves to DEMO scope. */
  public static boolean isDemoMode(Context ctx) {
    return getRaceScope(ctx) == RaceScope.DEMO;
  }
}
