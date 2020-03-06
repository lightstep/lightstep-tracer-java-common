package com.lightstep.tracer.metrics;

import java.io.IOException;

import oshi.hardware.HardwareAbstractionLayer;

abstract class MetricGroup {
  private final Metric<? extends MetricGroup,?>[] metrics;
  private long[] previous;

  @SafeVarargs
  MetricGroup(final Metric<? extends MetricGroup,?> ... metrics) {
    this.metrics = metrics;
    this.previous = new long[metrics.length];
  }

  abstract <I,O>long[] sample(Sender<I,O> sender, long timestampSeconds, long durationSeconds, I request, HardwareAbstractionLayer hal) throws IOException;

  <I,O>long[] execute(final Sender<I,O> sender, final long timestampSeconds, final long durationSeconds, final I request, final HardwareAbstractionLayer hal) throws IOException {
    final long[] current = sample(sender, timestampSeconds, durationSeconds, request, hal);
    for (int i = 0; i < metrics.length; ++i)
      sender.createMessage(request, timestampSeconds, durationSeconds, metrics[i], current[i], previous[i]);

    return this.previous = current;
  }

  final long[] getPrevious() {
    return previous;
  }
}