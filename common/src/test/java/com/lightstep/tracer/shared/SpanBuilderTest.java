package com.lightstep.tracer.shared;

import com.lightstep.tracer.grpc.KeyValue;
import com.lightstep.tracer.grpc.Span.Builder;
import io.opentracing.tag.Tag;
import io.opentracing.tag.Tags;
import io.opentracing.util.ThreadLocalScopeManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static io.opentracing.References.FOLLOWS_FROM;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SpanBuilderTest {
    private static final String OPERATION_NAME = "myOperation";
    private static final long TRACE_ID = 1;
    private static final long SPAN_ID = 2;

    @Mock
    private AbstractTracer tracer;

    private SpanContext context;
    private SpanBuilder undertest;
    private Map<String, String> baggageItems;

    @Before
    public void setup() {
        baggageItems = Collections.emptyMap();
        context = new SpanContext(TRACE_ID, SPAN_ID, baggageItems);
        undertest = new SpanBuilder(OPERATION_NAME, tracer);

        when(tracer.scopeManager()).thenReturn(new ThreadLocalScopeManager());
    }

    /**
     * Confirms aspects of the resulting Span when the builder is given a parent Span.
     */
    @Test
    public void testStart_asChildOfSpan() throws Exception {
        Span span = new Span(null, context, null, 0L);
        undertest.asChildOf(span);
        verifySettingsFromParent();
    }

    /**
     * Confirms aspects of the resulting Span when the builder is given a parent SpanContext.
     */
    @Test
    public void testStart_asChildOfSpanContext() throws Exception {
        undertest.asChildOf(context);
        verifySettingsFromParent();
    }

    /**
     * Confirms aspects of the resulting Span when the builder is given a parent SpanContext.
     */
    @Test
    public void testStart_asFollowsFromSpanContext() throws Exception {
        undertest.addReference(FOLLOWS_FROM, context);
        verifySettingsFromParent();
    }

    /**
     * Confirms the inherited baggage of the Span when the builder is given a parent SpanContext.
     */
    @Test
    public void testStart_ParentBaggage() {
        SpanContext parentContext = new SpanContext(TRACE_ID, SPAN_ID, new HashMap<String, String>() {{
            put("foo", "bar");
        }});

        undertest.asChildOf(parentContext);
        Span result = (Span)undertest.start();
        assertEquals("bar", result.context().getBaggageItem("foo"));

        result.setBaggageItem("another", "item");
        assertEquals("item", result.context().getBaggageItem("another"));
        assertNull(parentContext.getBaggageItem("another"));
    }

    @Test
    public void testStart_isDisabled() {
        when(tracer.isDisabled()).thenReturn(true);

        io.opentracing.Span result = undertest.start();
        assertSame(NoopSpan.INSTANCE, result);
    }

    /**
     * Confirms that the tags set on the builder are passed onto the Span.
     */
    @Test
    public void testTags() {
        // add one of each type of tag
        undertest.withTag("key1", "value1");
        undertest.withTag("key2", true);
        undertest.withTag("key3", 1001);
        undertest.withTag(Tags.COMPONENT, "mytest");

        // start the Span
        io.opentracing.Span otSpan = undertest.start();
        assertNotNull(otSpan);
        assertTrue(otSpan instanceof Span);
        Span lsSpan = (Span) otSpan;

        Builder record = lsSpan.getGrpcSpan();

        List<KeyValue> attributes = record.getTagsList();
        assertTrue(attributes.contains(KeyValue.newBuilder().setKey("key1").setStringValue("value1").build()));
        assertTrue(attributes.contains(KeyValue.newBuilder().setKey("key2").setBoolValue(true).build()));
        assertTrue(attributes.contains(KeyValue.newBuilder().setKey("key3").setIntValue(1001).build()));
        assertTrue(attributes.contains(KeyValue.newBuilder().setKey(Tags.COMPONENT.getKey()).setStringValue("mytest")
                    .build()));

        verifyResultingSpan(lsSpan);
    }

    @Test
    public void testTagNull() {
        undertest.withTag((Tag)null, "mytest");
        // start the Span
        io.opentracing.Span otSpan = undertest.start();
        assertNotNull(otSpan);
        assertTrue(otSpan instanceof Span);
        Span lsSpan = (Span) otSpan;

        Builder record = lsSpan.getGrpcSpan();

        List<KeyValue> attributes = record.getTagsList();
        assertEquals(0, attributes.size());
        verify(tracer).debug("tag (null) or value (mytest) is null, ignoring");
    }

    /**
     * When no parent is set, baggage items should be non-null and empty.
     */
    @Test
    public void testBaggageItems_noParent() {
        Iterable<Map.Entry<String, String>> result = undertest.baggageItems();
        assertNotNull(result);
        Iterator<Map.Entry<String, String>> iterator = result.iterator();
        assertFalse(iterator.hasNext());
    }

    /**
     * If no start timestamp is provided, the span's start time should be set to current and the
     * record's oldest micros should also be set to current.
     */
    @Test
    public void testStart_noStartTimeProvided() {
        // start the span
        io.opentracing.Span otSpan = undertest.start();
        assertNotNull(otSpan);
        assertTrue(otSpan instanceof Span);
        Span lsSpan = (Span) otSpan;

        assertTrue(lsSpan.getStartTimestampRelativeNanos() + " was not greater than zero",
                lsSpan.getStartTimestampRelativeNanos() > 0);
        assertTrue(lsSpan.getGrpcSpan().getStartTimestamp() + " was not greater than zero",
                Util.protoTimeToEpochMicros(lsSpan.getGrpcSpan().getStartTimestamp()) > 0);

        verifyResultingSpan(lsSpan);
    }

    /**
     * If start timestamp is provided, the span's start time should not be set and the record's
     * oldest micros should be set to the value provided.
     */
    @Test
    public void testStart_startTimeProvided() {
        undertest.withStartTimestamp(2002L);

        // start the span
        io.opentracing.Span otSpan = undertest.start();
        assertNotNull(otSpan);
        assertTrue(otSpan instanceof Span);
        Span lsSpan = (Span) otSpan;

        assertEquals(-1, lsSpan.getStartTimestampRelativeNanos());
        assertEquals(2002L, Util.protoTimeToEpochMicros(lsSpan.getGrpcSpan().getStartTimestamp()));

        verifyResultingSpan(lsSpan);
    }

    /**
     * If traceId and spanId are provided, they should be set on the span's context.
     */
    @Test
    public void testStart_traceIdAndSpanIdProvided() {
        undertest.withTraceIdAndSpanId(3, 4);

        // start the span
        io.opentracing.Span otSpan = undertest.start();
        assertNotNull(otSpan);
        assertTrue(otSpan instanceof Span);
        Span lsSpan = (Span) otSpan;

        assertEquals(3, lsSpan.context().getTraceId());
        assertEquals(4, lsSpan.context().getSpanId());

        verifyResultingSpan(lsSpan);
    }

    private void verifySettingsFromParent() {
        // verify that getBaggage returns baggage from parent
        Iterable<Map.Entry<String, String>> actualBaggageItems = undertest.baggageItems();
        assertEquals(baggageItems.entrySet(), actualBaggageItems);

        // start the span
        io.opentracing.Span otSpan = undertest.start();
        assertNotNull(otSpan);
        assertTrue(otSpan instanceof Span);
        Span lsSpan = (Span) otSpan;

        // verify that parent's trace id is set on context in returned span
        SpanContext spanContext = lsSpan.context();
        assertEquals(TRACE_ID, spanContext.getTraceId());

        verifyResultingSpan(lsSpan);
        assertEquals(TRACE_ID, context.getTraceId());
    }

    /**
     * Verify values that should be set on the result span regardless of other state.
     */
    private void verifyResultingSpan(Span resultingSpan) {
        Builder record = resultingSpan.getGrpcSpan();
        SpanContext context = resultingSpan.context();

        assertNotEquals(SPAN_ID, context.getSpanId());

        assertEquals(OPERATION_NAME, record.getOperationName());
        assertEquals(context.getTraceId(), record.getSpanContext().getTraceId());
        assertEquals(context.getSpanId(), record.getSpanContext().getSpanId());
    }
}
