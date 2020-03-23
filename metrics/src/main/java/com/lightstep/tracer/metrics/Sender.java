package com.lightstep.tracer.metrics;

import java.io.IOException;

public abstract class Sender<I,O> implements AutoCloseable {
  final String componentName;
  final String servicePath;
  final int servicePort;

  Sender(final String componentName, final String servicePath, final int servicePort) {
    this.componentName = componentName;
    this.servicePath = servicePath;
    this.servicePort = servicePort;
  }

  abstract <V extends Number>void createMessage(I request, long timestampSeconds, long durationSeconds, Metric<?,V> metric, long current, long previous) throws IOException;
  abstract I newRequest();
  abstract I setIdempotency(I request);
  abstract O invoke(I request, long timeout) throws Exception;

  private I request;
  private long previousTime = System.currentTimeMillis() / 1000;

  private String reporter;

  final O exec(final long timeout) throws Exception {
    final I request = getRequest();
    if (request == null)
      throw new IllegalStateException("Request should not be null");

    final O response = invoke(request, timeout);
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

  final void updateSampleRequest(final MetricGroup[] metricGroups) throws IOException {
    final long timestampSeconds = System.currentTimeMillis() / 1000;
    final long durationSeconds = timestampSeconds - getPreviousTimestamp();
    previousTime = timestampSeconds;

    final I request = setIdempotency(this.request != null ? this.request : newRequest());
    for (final MetricGroup metricGroup : metricGroups)
      metricGroup.execute(this, request, timestampSeconds, durationSeconds);

    this.request = request;
  }
}