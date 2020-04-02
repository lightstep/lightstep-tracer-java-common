package com.lightstep.tracer.metrics;

import java.util.HashMap;

import com.lightstep.tracer.grpc.MetricPoint;

abstract class ValueAdapter<V extends Number> {
  private static final HashMap<Class<?>,ValueAdapter<?>> instances = new HashMap<>();

  static final ValueAdapter<Long> LONG = new ValueAdapter<Long>(Long.class) {
    @Override
    long toLong(final Long value) {
      return value;
    }

    @Override
    Long toT(final long value) {
      return value;
    }

    @Override
    Long diff(final long current, final long previous) {
      return current - previous;
    }

    @Override
    void setValue(final MetricPoint.Builder builder, final Long value) {
      builder.setUint64Value(value);
    }
  };

  static final ValueAdapter<Double> DOUBLE = new ValueAdapter<Double>(Double.class) {
    @Override
    long toLong(final Double value) {
      return Double.doubleToLongBits(value);
    }

    @Override
    Double toT(final long value) {
      return Double.longBitsToDouble(value);
    }

    @Override
    Double diff(final long current, final long previous) {
      return toT(current) - toT(previous);
    }

    @Override
    void setValue(final MetricPoint.Builder builder, final Double value) {
      builder.setDoubleValue(value);
    }
  };

  abstract V diff(long current, long previous);
  abstract long toLong(V value);
  abstract V toT(long value);
  abstract void setValue(MetricPoint.Builder builder, V value);

  private ValueAdapter(final Class<V> type) {
    instances.put(type, this);
  }

  @SuppressWarnings("unchecked")
  static <T extends Number>ValueAdapter<T> get(final Class<T> type) {
    return (ValueAdapter<T>)instances.get(type);
  }
}