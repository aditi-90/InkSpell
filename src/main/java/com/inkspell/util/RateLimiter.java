package com.inkspell.util;

import java.util.concurrent.ConcurrentHashMap;

public final class RateLimiter {
  private record Window(long start, int count) {}

  private static final ConcurrentHashMap<String, Window> WINDOWS = new ConcurrentHashMap<>();

  private RateLimiter() {}

  public static synchronized boolean allow(String key, int limit, long interval) {
    long now = System.currentTimeMillis();
    if (WINDOWS.size() > 10000)
      WINDOWS.entrySet().removeIf(e -> now - e.getValue().start > 3600000);
    Window w = WINDOWS.get(key);
    if (w == null || now - w.start >= interval) {
      WINDOWS.put(key, new Window(now, 1));
      return true;
    }
    if (w.count >= limit) return false;
    WINDOWS.put(key, new Window(w.start, w.count + 1));
    return true;
  }
}
