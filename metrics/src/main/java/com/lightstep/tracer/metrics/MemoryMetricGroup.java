package com.lightstep.tracer.metrics;

import java.io.IOException;

import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;

class MemoryMetricGroup extends MetricGroup {
  MemoryMetricGroup(final HardwareAbstractionLayer hal) {
    super(hal, new GaugeMetric<>("mem.available", Long.class, 1), new GaugeMetric<>("mem.total", Long.class, 1));
  }

  @Override
  <I,O>long[] newSample() throws IOException {
    final GlobalMemory memory = hal.getMemory();
    return new long[] {memory.getAvailable(), memory.getTotal()};
  }
}