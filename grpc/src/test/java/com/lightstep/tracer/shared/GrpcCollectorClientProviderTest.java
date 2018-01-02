package com.lightstep.tracer.shared;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GrpcCollectorClientProviderTest {
    @Test
    public void testProvide() {
        assertNotNull(GrpcCollectorClientProvider.provider());
    }

    @Test
    public void testProvideBounded() {
        assertNotNull(CollectorClientProvider.provider());
        assertEquals(
                GrpcCollectorClientProvider.provider().getClass(),
                CollectorClientProvider.provider().getClass()
        );
    }
}
