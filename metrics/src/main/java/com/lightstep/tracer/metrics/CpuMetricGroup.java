package com.lightstep.tracer.metrics;

import java.io.IOException;

import oshi.hardware.CentralProcessor;
import oshi.hardware.CentralProcessor.TickType;
import oshi.hardware.HardwareAbstractionLayer;

class CpuMetricGroup extends MetricGroup {
  private static final GaugeMetric<CpuMetricGroup,Long> cpuPercent = new GaugeMetric<>("cpu.percent", Long.class);

  CpuMetricGroup() {
    super(new CounterMetric<>("cpu.user", Long.class), null, new CounterMetric<>("cpu.sys", Long.class), null, null, null, null, null, null, new GaugeMetric<>("cpu.percent", Long.class));
  }

  @Override
  <I,O>long[] sample(final Sender<I,O> sender, final long timestampSeconds, final long durationSeconds, final I request, final HardwareAbstractionLayer hal) throws IOException {
    final CentralProcessor processor = hal.getProcessor();
    final long[] ticks = processor.getSystemCpuLoadTicks();
    final long[] previous = getPrevious();
    final long user = ticks[TickType.USER.getIndex()] - previous[TickType.USER.getIndex()];
    final long nice = ticks[TickType.NICE.getIndex()] - previous[TickType.NICE.getIndex()];
    final long sys = ticks[TickType.SYSTEM.getIndex()] - previous[TickType.SYSTEM.getIndex()];
    final long idle = ticks[TickType.IDLE.getIndex()] - previous[TickType.IDLE.getIndex()];
    final long iowait = ticks[TickType.IOWAIT.getIndex()] - previous[TickType.IOWAIT.getIndex()];
    final long irq = ticks[TickType.IRQ.getIndex()] - previous[TickType.IRQ.getIndex()];
    final long softirq = ticks[TickType.SOFTIRQ.getIndex()] - previous[TickType.SOFTIRQ.getIndex()];
    final long steal = ticks[TickType.STEAL.getIndex()] - previous[TickType.STEAL.getIndex()];
    final long totalCpu = user + nice + sys + idle + iowait + irq + softirq + steal;

    System.err.println(String.format("CPU: Load: %.1f%% User: %.1f%% System: %.1f%%", hal.getProcessor().getSystemCpuLoadBetweenTicks(getPrevious()) * 100, 100d * user / totalCpu, 100d * sys / totalCpu));
    return ticks;
  }

  @Override
  <I,O>long[] execute(final Sender<I,O> sender, final long timestampSeconds, final long durationSeconds, final I request, final HardwareAbstractionLayer hal) throws IOException {
    final long cpuLoad = (long)(hal.getProcessor().getSystemCpuLoadBetweenTicks(getPrevious()) * 100);
    final long[] current = super.execute(sender, timestampSeconds, durationSeconds, request, hal);
    sender.createMessage(request, timestampSeconds, durationSeconds, cpuPercent, cpuLoad, 0L);
    return current;
  }
}