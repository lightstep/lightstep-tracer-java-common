package com.lightstep.tracer.shared;

import static org.junit.Assert.*;

import org.junit.Test;

import com.lightstep.tracer.shared.SafeMetrics;

public class SafeMetricsTest {
  @Test
  public void test() {
    if (System.getProperty("java.version").startsWith("1.8")) {
      System.err.println("This test is meant to be run with jdk1.7");
      return;
    }

    assertNull(SafeMetrics.getInstance(null, -1, null, -1));
  }
}