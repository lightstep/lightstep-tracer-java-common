package com.lightstep.tracer.metrics;

class GaugeMetric<G extends MetricGroup,T extends Number> extends Metric<G,T> {
  GaugeMetric(final String name, final Class<T> type) {
    super(name, type);
  }

  @Override
  T calculate(final long current, final long previous) {
    return adapter.toT(current);
  }
}