package com.lightstep.tracer.shared;

import static org.junit.Assert.*;

import org.junit.Test;

import com.lightstep.tracer.shared.SafeMetrics;

public class SafeMetricsTest {
  @Test
  public void test() {
    if (!System.getProperty("java.version").startsWith("1.7")) {
      System.err.println("This test is meant to be run with jdk1.7");
      return;
    }

    assertNull(SafeMetrics.getInstance(null, null, null, -1, 60));
  }
}
