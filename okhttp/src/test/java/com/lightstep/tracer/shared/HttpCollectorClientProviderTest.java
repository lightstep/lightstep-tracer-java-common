package com.lightstep.tracer.shared;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class HttpCollectorClientProviderTest {
    @Test
    public void testProvide() {
        assertNotNull(HttpCollectorClientProvider.provider());
    }

    @Test
    public void testProvideBounded() {
        CollectorClientProvider provider = CollectorClientProvider.provider(null, null);
        assertNotNull(provider);
        assertEquals(HttpCollectorClientProvider.provider().getClass(), provider.getClass());
    }
}
