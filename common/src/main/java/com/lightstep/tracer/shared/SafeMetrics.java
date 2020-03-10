package com.lightstep.tracer.shared;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lightstep.tracer.metrics.Metrics;

public class SafeMetrics {
  private static final Logger logger = LoggerFactory.getLogger(SafeMetrics.class);
  private static final boolean isJdk17 = System.getProperty("java.version").startsWith("1.7");

  public static Metrics getInstance(final String componentName, final int samplePeriodSeconds, final String hostName, final int port) {
    if (isJdk17) {
      logger.warn("Metrics supports jdk1.8+");
      return null;
    }

    return Metrics.getInstance(componentName, samplePeriodSeconds, hostName, port);
  }
}