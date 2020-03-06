package com.lightstep.tracer.metrics;

import java.io.IOException;

import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;

public class MemoryMetricGroup extends MetricGroup {
  MemoryMetricGroup(final HardwareAbstractionLayer hal) {
    super(hal, new GaugeMetric<>("mem.available", Long.class), new GaugeMetric<>("mem.total", Long.class));
  }

  @Override
  <I,O>long[] newSample(final Sender<I,O> sender, final long timestampSeconds, final long durationSeconds, final I request) throws IOException {
    final long[] current = new long[2];

    final GlobalMemory memory = hal.getMemory();
    current[0] = memory.getAvailable();
    current[1] = memory.getTotal();

    System.err.println("Memory [available/total]: " + current[0] + "/" + current[1]);
    return current;
  }
}