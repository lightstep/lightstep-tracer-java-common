package com.lightstep.tracer.shared;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class HttpCollectorClientProviderTest {
    @Test
    public void testProvide() {
        assertNotNull(HttpCollectorClientProvider.provider());
    }

    @Test
    public void testProvideBounded() {
        assertNotNull(CollectorClientProvider.provider());
        assertEquals(
                HttpCollectorClientProvider.provider().getClass(),
                CollectorClientProvider.provider().getClass()
        );
    }
}
