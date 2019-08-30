package com.lightstep.tracer.shared;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import io.opentracing.propagation.BinaryAdapters;
import io.opentracing.propagation.BinaryInject;
import java.nio.ByteBuffer;
import java.util.HashMap;
import org.junit.Test;

public class EnvoyPropagatorTest {
    final static EnvoyPropagator propagator = new EnvoyPropagator();

    @Test
    public void testInjectAndExtractIds() {
        SpanContext spanContext = new SpanContext(12345L, 67890L);
        ByteBuffer buff = ByteBuffer.allocate(128);
        propagator.inject(spanContext, BinaryAdapters.injectionCarrier(buff));

        buff.rewind();
        SpanContext result = propagator.extract(BinaryAdapters.extractionCarrier(buff));

        assertEquals(12345L, result.getTraceId());
        assertEquals(67890L, result.getSpanId());
    }

    @Test
    public void testInjectAndExtractFull() {
        SpanContext spanContext = new SpanContext(12345L, 67890L, new HashMap<String, String>() {{
            put("keyA", "valueA");
            put("keyZ", "valueZ");
        }});
        ByteBuffer buff = ByteBuffer.allocate(128);
        propagator.inject(spanContext, BinaryAdapters.injectionCarrier(buff));

        buff.rewind();
        SpanContext result = propagator.extract(BinaryAdapters.extractionCarrier(buff));

        assertEquals(12345L, result.getTraceId());
        assertEquals(67890L, result.getSpanId());
        assertEquals("valueA", result.getBaggageItem("keyA"));
        assertEquals("valueZ", result.getBaggageItem("keyZ"));
    }

    @Test
    public void testExtractEmpty() {
        ByteBuffer emptyBuff = ByteBuffer.wrap(new byte[0]);
        SpanContext result = propagator.extract(BinaryAdapters.injectionCarrier(emptyBuff));
        assertNull(result);
    }

    @Test
    public void testExtractInvalid() {
        ByteBuffer invalidBuff = ByteBuffer.wrap(new byte[] { 1, 7, 13 });
        SpanContext result = propagator.extract(BinaryAdapters.injectionCarrier(invalidBuff));
        assertNull(result);
    }
}
