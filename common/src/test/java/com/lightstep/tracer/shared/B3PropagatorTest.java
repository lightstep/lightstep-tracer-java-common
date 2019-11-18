package com.lightstep.tracer.shared;

import static org.junit.Assert.*;

import io.opentracing.propagation.TextMap;
import io.opentracing.propagation.TextMapExtractAdapter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Test;

public class B3PropagatorTest {

    @Test
    public void testExtract_mixedCaseIsLowered() {
        Map<String, String> mixedCaseHeaders = new HashMap<>();

        mixedCaseHeaders.put("X-B3-SpanId", Long.toHexString(1));
        mixedCaseHeaders.put("X-b3-traceId", Long.toHexString(2));
        mixedCaseHeaders.put("x-B3-sampled", "true");

        B3Propagator subject = new B3Propagator();

        SpanContext span = subject.extract(new TextMapExtractAdapter(mixedCaseHeaders));

        assertNotNull(span);
        assertEquals(span.getSpanId(), 1);
        assertEquals(span.getTraceId(), 2);
    }

    @Test
    public void testInjectAndExtractIds() {
        B3Propagator undertest = new B3Propagator();
        TextMap carrier = new TextMap() {
            final Map<String, String> textMap = new HashMap<>();

            public void put(String key, String value) {
                textMap.put(key, value);
            }

            public Iterator<Entry<String, String>> iterator() {
                return textMap.entrySet().iterator();
            }
        };
        SpanContext spanContext = new SpanContext();
        undertest.inject(spanContext, carrier);

        SpanContext result = undertest.extract(carrier);

        assertEquals(spanContext.getTraceId(), result.getTraceId());
        assertEquals(spanContext.getSpanId(), result.getSpanId());
    }

    @Test
    public void testExtract128BitTraceId() {
        String traceId = "463ac35c9f6413ad48485a3953bb6124";
        String spanId = "463ac35c9f6413ad";
        Map<String, String> headers = new HashMap<>();

        headers.put("X-B3-TraceId", traceId);
        headers.put("X-B3-SpanId", spanId);

        B3Propagator undertest = new B3Propagator();
        SpanContext result = undertest.extract(new TextMapExtractAdapter(headers));

        // `48485a3953bb6124` (the right-most part) from the trace id
        // should be used, which is `5208512171318403364` in decimal.
        assertEquals(5208512171318403364L, result.getTraceId());
        assertEquals(5060571933882717101L, result.getSpanId());
    }

    @Test
    public void testExtractEmptyHeaders() {
        Map<String, String> headers = new HashMap<>();

        headers.put("X-B3-SpanId", "");
        headers.put("X-B3-TraceId", "");

        B3Propagator subject = new B3Propagator();

        SpanContext span = subject.extract(new TextMapExtractAdapter(headers));

        assertNull(span);
    }

    @Test
    public void testExtractInvalidHeaders() {
        Map<String, String> headers = new HashMap<>();

        headers.put("X-B3-SpanId", "#$%");
        headers.put("X-B3-TraceId", "#$%");

        B3Propagator subject = new B3Propagator();

        SpanContext span = subject.extract(new TextMapExtractAdapter(headers));

        assertNull(span);
    }
}
