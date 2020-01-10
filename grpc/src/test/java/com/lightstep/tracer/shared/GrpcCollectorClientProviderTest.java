package com.lightstep.tracer.shared;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class GrpcCollectorClientProviderTest {
    @Test
    public void testProvide() {
        assertNotNull(GrpcCollectorClientProvider.provider());
    }

    @Test
    public void testProvideBounded() {
        assertNotNull(CollectorClientProvider.provider(null));
        assertEquals(
                GrpcCollectorClientProvider.provider().getClass(),
                CollectorClientProvider.provider(null).getClass()
        );
    }
}
