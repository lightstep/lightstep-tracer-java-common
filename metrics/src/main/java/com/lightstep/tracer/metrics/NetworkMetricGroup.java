package com.lightstep.tracer.metrics;

import java.io.IOException;

import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;

class NetworkMetricGroup extends MetricGroup {
  NetworkMetricGroup(final HardwareAbstractionLayer hal) {
    super(hal, new CounterMetric<>("net.bytes_sent", Long.class, 1), new CounterMetric<>("net.bytes_recv", Long.class, 1));
  }

  @Override
  <I,O>long[] newSample() throws IOException {
    final long[] current = new long[2];
    for (final NetworkIF networkIF : hal.getNetworkIFs()) {
      current[0] += networkIF.getBytesSent();
      current[1] += networkIF.getBytesRecv();
    }

    return current;
  }
}