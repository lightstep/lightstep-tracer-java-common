package com.lightstep.tracer.shared;

import static org.junit.Assert.*;

import io.opentracing.propagation.TextMap;
import io.opentracing.propagation.TextMapExtractAdapter;
import io.opentracing.propagation.TextMapInjectAdapter;
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
        mixedCaseHeaders.put("x-B3-sampled", "1");

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
    public void testInjectSampled() {
        B3Propagator undertest = new B3Propagator();
        Map<String, String> headers = new HashMap<>();

        SpanContext spanContext = new SpanContext();
        undertest.inject(spanContext, new TextMapInjectAdapter(headers));

        // Verify we use the new sampled value format.
        assertEquals(headers.get("X-B3-Sampled"), "1");
    }

    @Test
    public void testInjectAndExtract128BitTraceId() {
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

        headers.clear();
        undertest.inject(result, new TextMapInjectAdapter(headers));

        // Inject the original, non-truncated 128-bit traceId.
        assertEquals(traceId, headers.get("X-B3-TraceId"));
        assertEquals(spanId, headers.get("X-B3-SpanId"));
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

    @Test
    public void testExtractSampledOldFormat() {
        Map<String, String> headers = new HashMap<>();

        headers.put("X-B3-SpanId", "12345");
        headers.put("X-B3-TraceId", "67890");
        headers.put("X-B3-Sampled", "true"); // true/false instead of 0/1.

        B3Propagator subject = new B3Propagator();

        SpanContext span = subject.extract(new TextMapExtractAdapter(headers));

        // Although currently we do not use/propagate the (old) sampled value,
        // we want to make sure we always handle both the old and new formats.
        assertNotNull(span);
        assertEquals(span.toSpanId(), "12345");
        assertEquals(span.toTraceId(), "67890");
    }
}
