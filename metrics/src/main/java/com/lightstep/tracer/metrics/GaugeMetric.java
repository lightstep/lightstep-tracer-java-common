package com.lightstep.tracer.metrics;

class GaugeMetric<G extends MetricGroup,T extends Number> extends Metric<G,T> {
  GaugeMetric(final String name, final Class<T> type, final long factor) {
    super(name, type, factor);
  }

  @Override
  T compute(final long current, final long previous) {
    return getAdapter().toT(current);
  }
}