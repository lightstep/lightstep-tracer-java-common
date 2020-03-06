package com.lightstep.tracer.metrics;

import java.io.IOException;

import oshi.hardware.HardwareAbstractionLayer;

abstract class Sender<I,O> {
  final String componentName;

  Sender(final String componentName) {
    this.componentName = componentName;
  }

  abstract <V extends Number>void createMessage(I request, long timestampSeconds, long durationSeconds, Metric<?,V> metric, long current, long previous) throws IOException;
  abstract O run(MetricGroup[] metricGroups, HardwareAbstractionLayer hal, long timeout) throws Exception;

  private I request;
  private long previousTime = 0;

  I getRequest() {
    return this.request != null ? this.request : newRequest();
  }

  void setRequest(final I request) {
    this.request = request;
  }

  final long getPreviousTimestamp() {
    return previousTime;
  }

  abstract I newRequest();

  final I newSampleRequest(final MetricGroup[] metricGroups, final HardwareAbstractionLayer hal, final I request) throws IOException {
    final long timestampSeconds = System.currentTimeMillis() / 1000;
    final long durationSeconds = timestampSeconds - getPreviousTimestamp();
    for (final MetricGroup metricGroup : metricGroups)
      metricGroup.execute(this, timestampSeconds, durationSeconds, request, hal);

    return request;
  }
}