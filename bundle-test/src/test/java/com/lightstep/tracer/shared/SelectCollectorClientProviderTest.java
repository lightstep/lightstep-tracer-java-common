package com.lightstep.tracer.shared;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class SelectCollectorClientProviderTest {

  @Test
  public void testProvider() {
    assertProvider(GrpcCollectorClientProvider.provider(), null);
    assertProvider(GrpcCollectorClientProvider.provider(), Options.ClientProvider.GRPC);
    assertProvider(HttpCollectorClientProvider.provider(), Options.ClientProvider.HTTP);
  }

  private void assertProvider(CollectorClientProvider expected, Options.ClientProvider provided) {
    CollectorClientProvider provider = CollectorClientProvider.provider(provided);
    assertNotNull(provider);
    assertEquals(expected.getClass(), provider.getClass());
  }
}
