package com.lightstep.tracer.metrics;

import static org.junit.Assert.*;

import org.junit.Test;

public class MetricsProviderImplTest {
  @Test
  public void test() {
      MetricsProviderImpl provider = new MetricsProviderImpl();
      SafeMetricsImpl safeMetrics = (SafeMetricsImpl) provider.create();
      assertNotNull(safeMetrics);

      Thread metricsThread = safeMetrics.createMetricsThread("", "", "https://localhost", 464);
      assertNotNull(metricsThread);
      assertNotSame(metricsThread, safeMetrics.createMetricsThread("", "", "https://localhost", 464));
  }
}
