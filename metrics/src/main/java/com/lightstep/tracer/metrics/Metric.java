package com.lightstep.tracer.metrics;

import java.util.Objects;

abstract class Metric<G extends MetricGroup,V extends Number> {
  private final ValueAdapter<V> adapter;
  private final String name;
  private final long factor;

  Metric(final String name, final Class<V> type, final long factor) {
    this.name = name;
    this.factor = factor;
    this.adapter = Objects.requireNonNull(ValueAdapter.get(type), type.getName());
  }

  final ValueAdapter<V> getAdapter() {
    return this.adapter;
  }

  final String getName() {
    return this.name;
  }

  abstract V compute(long current, long previous);

  final double getValue(final long current, final long previous) {
    return compute(current, previous).doubleValue() / factor;
  }
}