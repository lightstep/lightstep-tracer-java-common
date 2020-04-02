package com.lightstep.tracer.shared;

import java.util.Iterator;
import java.util.ServiceLoader;

public abstract class MetricsProvider {
  public static MetricsProvider provider() {
      Iterator<MetricsProvider> iter = ServiceLoader.load(MetricsProvider.class).iterator();
      return iter.hasNext() ? iter.next() : NopMetricsProvider.INSTANCE;
  }

  private static class NopMetricsProvider extends MetricsProvider {
      private static final NopMetricsProvider INSTANCE = new NopMetricsProvider();

      private NopMetricsProvider() {}

      @Override
      public SafeMetrics create() {
          return null;
      }
  }

  public abstract SafeMetrics create();
}
