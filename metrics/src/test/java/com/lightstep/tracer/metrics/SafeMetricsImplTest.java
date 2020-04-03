package com.lightstep.tracer.metrics;

import static org.junit.Assert.*;

import org.junit.Test;

public class SafeMetricsImplTest {
  @Test
  public void test() {
    if (!System.getProperty("java.version").startsWith("1.7")) {
      System.err.println("This test is meant to be run with jdk1.7");
      return;
    }

    assertNull(new SafeMetricsImpl().createMetricsThread(null, null, null, null, 60));
  }
}
