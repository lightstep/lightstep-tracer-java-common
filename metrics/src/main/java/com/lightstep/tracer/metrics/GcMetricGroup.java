package com.lightstep.tracer.metrics;

import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;

import oshi.hardware.HardwareAbstractionLayer;

class GcMetricGroup extends MetricGroup {
  GcMetricGroup(final HardwareAbstractionLayer hal) {
    super(hal, new GaugeMetric<>("runtime.mem.heap_size", Long.class), new CounterMetric<>("runtime.gc.count", Long.class), new CounterMetric<>("runtime.gc.time", Long.class));
  }

  @Override
  <I,O> long[] newSample(final Sender<I,O> sender, final long timestampSeconds, final long durationSeconds, final I request) throws IOException {
    long totalCount = 0;
    long totalTime = 0;
    for (final GarbageCollectorMXBean bean : ManagementFactory.getGarbageCollectorMXBeans()) {
      final long count = bean.getCollectionCount();
      if (count > 0)
        totalCount += count;

      long time = bean.getCollectionTime();
      if (time > 0)
        totalTime += time;
    }

    final long heapSize = Runtime.getRuntime().totalMemory();
    return new long[] {heapSize, totalCount, totalTime};
  }
}