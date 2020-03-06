package com.lightstep.tracer.metrics;

import java.io.IOException;

import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;

public class MemoryMetricGroup extends MetricGroup {
  MemoryMetricGroup() {
    super(new GaugeMetric<>("mem.available", Long.class), new GaugeMetric<>("mem.total", Long.class));
  }

  @Override
  <I,O>long[] sample(final Sender<I,O> sender, final long timestampSeconds, final long durationSeconds, final I request, final HardwareAbstractionLayer hal) throws IOException {
    final long[] current = new long[2];

    final GlobalMemory memory = hal.getMemory();
    current[0] = memory.getAvailable();
    current[1] = memory.getTotal();

    System.err.println("Memory [available/total]: " + current[0] + "/" + current[1]);
    return current;
  }
}