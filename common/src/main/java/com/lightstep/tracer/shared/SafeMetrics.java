package com.lightstep.tracer.shared;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lightstep.tracer.metrics.GrpcSender;
import com.lightstep.tracer.metrics.Metrics;
import com.lightstep.tracer.metrics.OkHttpSender;
import com.lightstep.tracer.metrics.Sender;

public class SafeMetrics {
  private static final Logger logger = LoggerFactory.getLogger(SafeMetrics.class);
  private static final boolean isJdk17 = System.getProperty("java.version").startsWith("1.7");

  public static Metrics getInstance(final Options.CollectorClient collectorClient, final String componentName, final String accessToken, final int samplePeriodSeconds, final String servicePath, final int port) {
    if (isJdk17) {
      logger.warn("Metrics supports jdk1.8+");
      return null;
    }

    final Sender<?,?> sender;
    if (collectorClient == Options.CollectorClient.GRPC) {
      sender = new GrpcSender(componentName, servicePath, port);
    } else { // Default to OkHttp
      sender = new OkHttpSender(samplePeriodSeconds * 1000, componentName, accessToken, servicePath, port);
    }

    return Metrics.getInstance(sender, samplePeriodSeconds);
  }
}
