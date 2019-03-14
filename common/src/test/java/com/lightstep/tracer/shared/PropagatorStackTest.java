package com.lightstep.tracer.shared;

import io.opentracing.propagation.Format;
import io.opentracing.propagation.Format.Builtin;
import io.opentracing.propagation.TextMapExtract;
import io.opentracing.propagation.TextMapExtractAdapter;
import io.opentracing.propagation.TextMapInject;
import io.opentracing.propagation.TextMapInjectAdapter;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PropagatorStackTest {
    @Test(expected = IllegalArgumentException.class)
    public void testCtor_NullFormat() {
        new PropagatorStack(null);
    }

    @Test
    public void testCtor_empty()
    {
        PropagatorStack propagatorStack = new PropagatorStack(Builtin.TEXT_MAP);
        assertTrue(propagatorStack.propagators.isEmpty());
        assertEquals(propagatorStack.format(), Builtin.TEXT_MAP);
    }

    @Test
    public void testCtor_customFormat() {
        Format format = new Format() {};
        PropagatorStack propagatorStack= new PropagatorStack(format);
        assertTrue(propagatorStack.propagators.isEmpty());
        assertEquals(propagatorStack.format(), format);
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

        assertEquals(propagatorStack.pushPropagator(Propagator.HTTP_HEADERS), propagatorStack);
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
        propagatorStack.pushPropagator(Propagator.HTTP_HEADERS);
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
        propagatorStack.pushPropagator(Propagator.HTTP_HEADERS);
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
        SpanContext context = new SpanContext();
        Map<String, String> carrier = new HashMap<>();
        Propagator stubPropagator = new StubPropagator();
        stubPropagator.inject(context, new TextMapInjectAdapter(carrier));

        PropagatorStack propagatorStack = new PropagatorStack(Builtin.HTTP_HEADERS);
        assertNull(propagatorStack.extract(new TextMapExtractAdapter(carrier)));

        propagatorStack.pushPropagator(Propagator.HTTP_HEADERS); // no compatible still.
        assertNull(propagatorStack.extract(new TextMapExtractAdapter(carrier)));
    }

    static class StubPropagator implements Propagator {
        public final static String STUB_TRACE_ID = "stub_trace_id";
        public final static String STUB_SPAN_ID = "stub_span_id";

        public <C> void inject(SpanContext context, C carrier) {
            TextMapInject textCarrier = (TextMapInject) carrier;
            textCarrier.put(STUB_TRACE_ID, Long.toString(context.getTraceId()));
            textCarrier.put(STUB_SPAN_ID, Long.toString(context.getSpanId()));
        }

        public <C> SpanContext extract(C carrier) {
            Long traceId = null;
            Long spanId = null;

            for (Map.Entry<String, String> entry: (TextMapExtract)carrier) {
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
