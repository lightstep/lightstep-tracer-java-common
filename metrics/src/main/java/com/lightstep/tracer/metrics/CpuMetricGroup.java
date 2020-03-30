package com.lightstep.tracer.metrics;

import java.io.IOException;

import oshi.hardware.CentralProcessor.TickType;
import oshi.hardware.HardwareAbstractionLayer;

class CpuMetricGroup extends MetricGroup {
  private static long sum(final long[] ticks) {
    long total = 0;
    for (int i = 0; i < ticks.length; ++i)
      total += ticks[i];

    return total;
  }

  private static final CounterMetric<CpuMetricGroup,Long> cpuUsage = new CounterMetric<>("cpu.usage", Long.class, 1000);
  private static final CounterMetric<CpuMetricGroup,Long> cpuTotal = new CounterMetric<>("cpu.total", Long.class, 1000);

  CpuMetricGroup(final HardwareAbstractionLayer hal) {
    super(hal, new CounterMetric<>("cpu.user", Long.class, 1000), null, new CounterMetric<>("cpu.sys", Long.class, 1000), null, null, null, null, null);
  }

  @Override
  <I,O>long[] newSample() throws IOException {
    return hal.getProcessor().getSystemCpuLoadTicks();
  }

  @Override
  <I,O>long[] execute(final Sender<I,O> sender, final I request, final long timestampSeconds, final long durationSeconds) throws IOException {
    final long[] previous = getPrevious();
    final long previousTotal = sum(previous);
    final long previousUsage = previousTotal - previous[TickType.IDLE.getIndex()];

    final long[] current = super.execute(sender, request, timestampSeconds, durationSeconds);
    final long currentTotal = sum(current);
    final long currentUsage = currentTotal - current[TickType.IDLE.getIndex()];

    if (logger.isDebugEnabled()) {
      logger.debug("'-- " + cpuUsage.getName() + "[" + (currentUsage - previousUsage) + "]");
      logger.debug("'-- " + cpuTotal.getName() + "[" + (currentTotal - previousTotal) + "]");
    }

    sender.createMessage(request, timestampSeconds, durationSeconds, cpuTotal, currentTotal, previousTotal);
    sender.createMessage(request, timestampSeconds, durationSeconds, cpuUsage, currentUsage, previousUsage);
    return current;
  }
}