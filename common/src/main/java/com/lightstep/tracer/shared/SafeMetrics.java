package com.lightstep.tracer.shared;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lightstep.tracer.metrics.Metrics;
import com.lightstep.tracer.metrics.OkHttpSender;
import com.lightstep.tracer.metrics.Sender;

public class SafeMetrics {
  private static final Logger logger = LoggerFactory.getLogger(SafeMetrics.class);
  private static final boolean isJdk17 = System.getProperty("java.version").startsWith("1.7");

  public static Metrics getInstance(final String componentName, final String accessToken,
        final String serviceUrl, final int samplePeriodSeconds) {
    if (isJdk17) {
      logger.warn("Metrics supports jdk1.8+");
      return null;
    }

    // TODO: Can we unify samplePeriodSeconds in a single place?
    Sender<?,?> sender = new OkHttpSender(componentName, accessToken,
          serviceUrl, samplePeriodSeconds * 1000);
    return Metrics.getInstance(sender, samplePeriodSeconds);
  }
}
