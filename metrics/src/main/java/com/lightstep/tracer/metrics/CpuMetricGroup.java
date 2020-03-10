package com.lightstep.tracer.metrics;

import java.io.IOException;

import oshi.hardware.HardwareAbstractionLayer;

class CpuMetricGroup extends MetricGroup {
  private static final GaugeMetric<CpuMetricGroup,Long> cpuPercent = new GaugeMetric<>("cpu.percent", Long.class);

  CpuMetricGroup(final HardwareAbstractionLayer hal) {
    super(hal, new CounterMetric<>("cpu.user", Long.class), null, new CounterMetric<>("cpu.sys", Long.class), null, null, null, null, null);
  }

  @Override
  <I,O>long[] newSample(final Sender<I,O> sender, final long timestampSeconds, final long durationSeconds, final I request) throws IOException {
    return hal.getProcessor().getSystemCpuLoadTicks();
  }

  @Override
  <I,O>long[] execute(final Sender<I,O> sender, final long timestampSeconds, final long durationSeconds, final I request) throws IOException {
    final long cpuLoad = (long)(hal.getProcessor().getSystemCpuLoadBetweenTicks(getPrevious()) * 100);
    final long[] current = super.execute(sender, timestampSeconds, durationSeconds, request);
    if (logger.isDebugEnabled())
      logger.debug("'-- " + cpuPercent.getName() + "[" + cpuLoad + "]");
    sender.createMessage(request, timestampSeconds, durationSeconds, cpuPercent, cpuLoad, 0L);
    return current;
  }
}