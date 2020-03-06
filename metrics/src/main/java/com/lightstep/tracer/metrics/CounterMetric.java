package com.lightstep.tracer.metrics;

class CounterMetric<G extends MetricGroup,T extends Number> extends Metric<G,T> {
  CounterMetric(final String name, final Class<T> type) {
    super(name, type);
  }

  @Override
  T compute(final long current, final long previous) {
    return adapter.diff(current, previous);
  }
}