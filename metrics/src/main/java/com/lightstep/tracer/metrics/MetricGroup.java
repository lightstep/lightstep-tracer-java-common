package com.lightstep.tracer.metrics;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oshi.hardware.HardwareAbstractionLayer;

abstract class MetricGroup {
  static final Logger logger = LoggerFactory.getLogger(CpuMetricGroup.class);
  final HardwareAbstractionLayer hal;
  private final Metric<? extends MetricGroup,?>[] metrics;
  private long[] previous;

  @SafeVarargs
  MetricGroup(final HardwareAbstractionLayer hal, final Metric<? extends MetricGroup,?> ... metrics) {
    this.hal = hal;
    this.metrics = metrics;
    this.previous = new long[metrics.length];
  }

  final long[] getPrevious() {
    return previous;
  }

  abstract <I,O>long[] newSample(Sender<I,O> sender, I request, long timestampSeconds, long durationSeconds) throws IOException;

  <I,O>long[] execute(final Sender<I,O> sender, final I request, final long timestampSeconds, final long durationSeconds) throws IOException {
    final long[] current = newSample(sender, request, timestampSeconds, durationSeconds);
    if (logger.isDebugEnabled())
    logger.debug(getClass().getSimpleName());
    for (int i = 0; i < metrics.length; ++i) {
      if (metrics[i] != null) {
        if (logger.isDebugEnabled()) {
          final Object value = metrics[i].compute(current[i], previous[i]);
          logger.debug("'-- " + metrics[i].getName() + "[" + value + "]");
        }

        sender.createMessage(request, timestampSeconds, durationSeconds, metrics[i], current[i], previous[i]);
      }
    }

    return this.previous = current;
  }
}