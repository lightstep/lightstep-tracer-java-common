package com.lightstep.tracer.metrics;

import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;

import oshi.hardware.HardwareAbstractionLayer;

class GcMetricGroup extends MetricGroup {
  GcMetricGroup(final HardwareAbstractionLayer hal) {
    super(hal, new GaugeMetric<>("runtime.java.heap_size", Long.class, 1), new CounterMetric<>("runtime.java.gc.count", Long.class, 1), new CounterMetric<>("runtime.java.gc.time", Long.class, 1000));
  }

  @Override
  <I,O>long[] newSample() throws IOException {
    long totalCount = 0;
    long totalTime = 0;
    for (final GarbageCollectorMXBean bean : ManagementFactory.getGarbageCollectorMXBeans()) {
      final long count = bean.getCollectionCount();
      if (count > 0)
        totalCount += count;

      final long time = bean.getCollectionTime();
      if (time > 0)
        totalTime += time;
    }

    final long heapSize = Runtime.getRuntime().totalMemory();
    return new long[] {heapSize, totalCount, totalTime};
  }
}