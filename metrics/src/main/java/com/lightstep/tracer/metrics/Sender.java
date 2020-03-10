package com.lightstep.tracer.metrics;

import java.io.IOException;

abstract class Sender<I,O> implements AutoCloseable {
  final String componentName;
  final String hostName;
  final int port;

  Sender(final String componentName, final String hostName, final int port) {
    this.componentName = componentName;
    this.hostName = hostName;
    this.port = port;
  }

  abstract <V extends Number>void createMessage(I request, long timestampSeconds, long durationSeconds, Metric<?,V> metric, long current, long previous) throws IOException;
  abstract O invoke(long timeout) throws Exception;

  private I request;
  private long previousTime = System.currentTimeMillis() / 1000;

  private String reporter;

  final O exec(final long timeout) throws Exception {
    final O response = invoke(timeout);
    setRequest(null);
    return response;
  }

  final String getPlatformReporter() {
    if (reporter != null)
      return reporter;

    final Package pkg = Sender.class.getPackage();
    final String groupId = pkg.getName();
    final String artifactId = pkg.getImplementationTitle();
    if (groupId == null)
      reporter = "test";
    else
      reporter = groupId + ":" + artifactId;

    return reporter;
  }

  private String version = null;

  final String getPlatformVersion() {
    if (version != null)
      return version;

    version = Sender.class.getPackage().getImplementationVersion();
    if (version == null)
      version = "0.0.0";

    return version;
  }

  final I getRequest() {
    return this.request;
  }

  final void setRequest(final I request) {
    this.request = request;
  }

  final long getPreviousTimestamp() {
    return previousTime;
  }

  abstract I newRequest();

  final void updateSampleRequest(final MetricGroup[] metricGroups) throws IOException {
    final I request = this.request != null ? this.request : newRequest();
    final long timestampSeconds = System.currentTimeMillis() / 1000;
    final long durationSeconds = timestampSeconds - getPreviousTimestamp();
    previousTime = timestampSeconds;
    for (final MetricGroup metricGroup : metricGroups)
      metricGroup.execute(this, timestampSeconds, durationSeconds, request);

    setRequest(request);
  }
}