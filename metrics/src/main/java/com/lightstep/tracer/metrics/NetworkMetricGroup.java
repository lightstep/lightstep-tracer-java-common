package com.lightstep.tracer.metrics;

import java.io.IOException;

import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;

public class NetworkMetricGroup extends MetricGroup {
  NetworkMetricGroup() {
    super(new CounterMetric<>("net.bytes_sent", Long.class), new CounterMetric<>("net.bytes_recv", Long.class));
  }

  @Override
  <I,O>long[] sample(final Sender<I,O> sender, final long timestampSeconds, final long durationSeconds, final I request, final HardwareAbstractionLayer hal) throws IOException {
    final long[] current = new long[2];
    for (final NetworkIF networkIF : hal.getNetworkIFs()) {
      current[0] += networkIF.getBytesSent();
      current[1] += networkIF.getBytesRecv();
    }

    System.err.println("Traffic [sent/received]: " + current[0] + "/" + current[1]);
    return current;
  }
}