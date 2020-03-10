package com.lightstep.tracer.metrics;

import java.io.IOException;

import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;

class MemoryMetricGroup extends MetricGroup {
  MemoryMetricGroup(final HardwareAbstractionLayer hal) {
    super(hal, new GaugeMetric<>("mem.available", Long.class), new GaugeMetric<>("mem.total", Long.class));
  }

  @Override
  <I,O>long[] newSample(final Sender<I,O> sender, final long timestampSeconds, final long durationSeconds, final I request) throws IOException {
    final GlobalMemory memory = hal.getMemory();
    return new long[] {memory.getAvailable(), memory.getTotal()};
  }
}