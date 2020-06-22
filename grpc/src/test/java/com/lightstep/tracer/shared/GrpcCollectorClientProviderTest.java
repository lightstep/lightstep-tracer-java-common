package com.lightstep.tracer.shared;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class GrpcCollectorClientProviderTest {
    @Test
    public void testProvide() {
        assertNotNull(GrpcCollectorClientProvider.provider());
    }

    @Test
    public void testProvideBounded() {
        CollectorClientProvider provider = CollectorClientProvider.provider(null, null);
        assertNotNull(provider);
        assertEquals(GrpcCollectorClientProvider.provider().getClass(), provider.getClass());
    }

    @Test
    public void testPreferredCollectorClientNotAvailable() {
        final List<String> log = new ArrayList<>();
        CollectorClientProvider provider = CollectorClientProvider.provider(
            Options.CollectorClient.HTTP, new Warner() {
                @Override
                public void warn(String message) {
                    log.add(message);
                }

                @Override
                public void error(String message) {
                    log.add(message);
                }
            });
        assertNotNull(provider);
        assertEquals(GrpcCollectorClientProvider.provider().getClass(), provider.getClass());
        assertEquals(Collections.singletonList(
            "expected HTTP collector client was not present in classpath. Using GRPC instead."), log);
    }
}
