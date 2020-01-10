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
        assertNotNull(CollectorClientProvider.provider(null));
        assertEquals(
                HttpCollectorClientProvider.provider().getClass(),
                CollectorClientProvider.provider(null).getClass()
        );
    }
}
