package com.lightstep.tracer.shared;

public interface SafeMetrics {
  Thread createMetricsThread(String componentName, String accessToken, String serviceUrl, int samplePeriodSeconds);
}
