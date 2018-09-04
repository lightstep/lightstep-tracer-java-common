package com.lightstep.tracer.shared;

import io.opentracing.propagation.Format;
import io.opentracing.propagation.Format.Builtin;
import io.opentracing.propagation.TextMap;
import io.opentracing.propagation.TextMapExtractAdapter;
import io.opentracing.propagation.TextMapInjectAdapter;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class PropagatorStackTest {
    @Test(expected = IllegalArgumentException.class)
    public void testCtor_NullFormat() {
        new PropagatorStack(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCtor_notRecognizedFormat() {
        Format format = new Format() {};
        new PropagatorStack(format);
    }

    @Test
    public void testCtor_TextMap() {
        PropagatorStack propagatorStack = new PropagatorStack(Builtin.TEXT_MAP);
        assertEquals(propagatorStack.format(), Builtin.TEXT_MAP);
        assertEquals(propagatorStack.propagators.size(), 1);
        assertEquals(propagatorStack.propagators.get(0), Propagator.TEXT_MAP);
    }

    @Test
    public void testCtor_HttpHeaders() {
        PropagatorStack propagatorStack = new PropagatorStack(Builtin.HTTP_HEADERS);
        assertEquals(propagatorStack.format(), Builtin.HTTP_HEADERS);
        assertEquals(propagatorStack.propagators.size(), 1);
        assertEquals(propagatorStack.propagators.get(0), Propagator.HTTP_HEADERS);
    }

    @Test
    public void testCtor_Binary() {
        PropagatorStack propagatorStack = new PropagatorStack(Builtin.BINARY);
        assertEquals(propagatorStack.format(), Builtin.BINARY);
        assertEquals(propagatorStack.propagators.size(), 1);
        assertEquals(propagatorStack.propagators.get(0), Propagator.BINARY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPushPropagator_Null() {
        PropagatorStack propagatorStack = new PropagatorStack(Builtin.HTTP_HEADERS);
        propagatorStack.pushPropagator(null);
    }

    @Test
    public void testPushPropagator() {
        Propagator b3Propagator = new B3Propagator();
        PropagatorStack propagatorStack = new PropagatorStack(Builtin.HTTP_HEADERS);
        assertEquals(propagatorStack.pushPropagator(b3Propagator), propagatorStack);
        assertEquals(propagatorStack.propagators.size(), 2);
        assertEquals(propagatorStack.propagators.get(0), Propagator.HTTP_HEADERS);
        assertEquals(propagatorStack.propagators.get(1), b3Propagator);
    }

    /**
     * Inject once with PropagatorStack, and extract individually with each
     * Propagator, to make sure all was injected properly.*/
    @Test
    public void testInject_allPropagators() {
        PropagatorStack propagatorStack = new PropagatorStack(Builtin.HTTP_HEADERS);
        Propagator b3Propagator = new B3Propagator();
        Propagator stubPropagator = new StubPropagator();
        propagatorStack.pushPropagator(b3Propagator);
        propagatorStack.pushPropagator(stubPropagator);

        Map<String, String> carrier = new HashMap<>();
        SpanContext context = new SpanContext();
        propagatorStack.inject(context, new TextMapInjectAdapter(carrier));

        Propagator[] propagators = new Propagator[] {
            Propagator.HTTP_HEADERS,
            b3Propagator,
            stubPropagator
        };
        for (int i = 0; i < propagators.length; i++) {
            SpanContext extracted = propagators[i].extract(new TextMapExtractAdapter(carrier));
            assertNotNull(extracted);
            assertEquals(context.getTraceId(), extracted.getTraceId());
            assertEquals(context.getSpanId(), extracted.getSpanId());
        }
    }

    /**
     * Inject individually with each Propagator, and extract with PropagatorStack
     * for each one of them, to verify we properly fallback to all of them.*/
    @Test
    public void testExtract_allPropagators() {
        PropagatorStack propagatorStack = new PropagatorStack(Builtin.HTTP_HEADERS);
        Propagator b3Propagator = new B3Propagator();
        Propagator stubPropagator = new StubPropagator();
        propagatorStack.pushPropagator(b3Propagator);
        propagatorStack.pushPropagator(stubPropagator);

        SpanContext context = new SpanContext();
        Propagator[] propagators = new Propagator[] {
            Propagator.HTTP_HEADERS,
            b3Propagator,
            stubPropagator
        };
        for (int i = 0; i < propagators.length; i++) {
            Map<String, String> carrier = new HashMap<>();
            propagators[i].inject(context, new TextMapInjectAdapter(carrier));

            SpanContext extracted = propagatorStack.extract(new TextMapExtractAdapter(carrier));
            assertNotNull(extracted);
            assertEquals(context.getTraceId(), extracted.getTraceId());
            assertEquals(context.getSpanId(), extracted.getSpanId());
        }
    }

    @Test
    public void testExtract_noneFound() {
        PropagatorStack propagatorStack = new PropagatorStack(Builtin.HTTP_HEADERS);

        Map<String, String> carrier = new HashMap<>();
        SpanContext context = new SpanContext();
        Propagator stubPropagator = new StubPropagator();
        stubPropagator.inject(context, new TextMapInjectAdapter(carrier));

        assertNull(propagatorStack.extract(new TextMapExtractAdapter(carrier)));
    }

    static class StubPropagator implements Propagator<TextMap> {
        public final static String STUB_TRACE_ID = "stub_trace_id";
        public final static String STUB_SPAN_ID = "stub_span_id";

        public void inject(SpanContext context, TextMap carrier) {
            carrier.put(STUB_TRACE_ID, Long.toString(context.getTraceId()));
            carrier.put(STUB_SPAN_ID, Long.toString(context.getSpanId()));
        }

        public SpanContext extract(TextMap carrier) {
            Long traceId = null;
            Long spanId = null;

            for (Map.Entry<String, String> entry: carrier) {
                if (entry.getKey().equals(STUB_TRACE_ID)) {
                    traceId = Long.valueOf(entry.getValue());
                } else if (entry.getKey().equals(STUB_SPAN_ID)) {
                    spanId = Long.valueOf(entry.getValue());
                }
            }

            if (traceId == null || spanId == null)
                return null;

            return new SpanContext(traceId, spanId);
        }
    }
}
