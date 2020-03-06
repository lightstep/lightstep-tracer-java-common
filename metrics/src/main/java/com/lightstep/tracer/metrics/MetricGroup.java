package com.lightstep.tracer.metrics;

import java.io.IOException;

import oshi.hardware.HardwareAbstractionLayer;

abstract class MetricGroup {
  private final Metric<? extends MetricGroup,?>[] metrics;
  private long[] previous;
  final HardwareAbstractionLayer hal;

  @SafeVarargs
  MetricGroup(final HardwareAbstractionLayer hal, final Metric<? extends MetricGroup,?> ... metrics) {
    this.hal = hal;
    this.metrics = metrics;
    this.previous = new long[metrics.length];
  }

  abstract <I,O>long[] newSample(Sender<I,O> sender, long timestampSeconds, long durationSeconds, I request) throws IOException;

  <I,O>long[] execute(final Sender<I,O> sender, final long timestampSeconds, final long durationSeconds, final I request) throws IOException {
    final long[] current = newSample(sender, timestampSeconds, durationSeconds, request);
    for (int i = 0; i < metrics.length; ++i)
      sender.createMessage(request, timestampSeconds, durationSeconds, metrics[i], current[i], previous[i]);

    return this.previous = current;
  }

  final long[] getPrevious() {
    return previous;
  }
}