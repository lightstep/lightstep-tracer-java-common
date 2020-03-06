package com.lightstep.tracer.metrics;

abstract class Metric<G extends MetricGroup,V extends Number> {
  ValueAdapter<V> adapter;
  final String name;

  Metric(final String name, final Class<V> type) {
    this.name = name;
    this.adapter = ValueAdapter.get(type);
  }

  abstract V compute(long current, long previous);
}