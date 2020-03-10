package com.lightstep.tracer.metrics;

import java.util.Objects;

abstract class Metric<G extends MetricGroup,V extends Number> {
  private final ValueAdapter<V> adapter;
  private final String name;

  Metric(final String name, final Class<V> type) {
    this.name = name;
    this.adapter = Objects.requireNonNull(ValueAdapter.get(type), type.getName());
  }

  final ValueAdapter<V> getAdapter() {
    return this.adapter;
  }

  final String getName() {
    return this.name;
  }

  abstract V compute(long current, long previous);
}