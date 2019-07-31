package com.lightstep.tracer.shared;

import com.lightstep.tracer.grpc.Reference;
import com.lightstep.tracer.grpc.Reference.Relationship;
import io.opentracing.Scope;
import io.opentracing.Tracer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static io.opentracing.References.CHILD_OF;
import static io.opentracing.References.FOLLOWS_FROM;

public class SpanBuilder implements Tracer.SpanBuilder {

    private final String operationName;
    private final Map<String, String> stringTags;
    private final Map<String, Boolean> boolTags;
    private final Map<String, Number> numTags;
    private final AbstractTracer tracer;

    private Long traceId = null;
    private Long spanId = null;
    private SpanContext parent;
    private long startTimestampMicros;
    private boolean ignoringActiveSpan;

    private final com.lightstep.tracer.grpc.Span.Builder grpcSpan = com.lightstep.tracer.grpc.Span.newBuilder();

    SpanBuilder(String operationName, AbstractTracer tracer) {
        this.operationName = operationName;
        this.tracer = tracer;
        stringTags = new HashMap<>();
        boolTags = new HashMap<>();
        numTags = new HashMap<>();
    }

    @Override
    public Tracer.SpanBuilder asChildOf(io.opentracing.SpanContext parent) {
        return addReference(CHILD_OF, parent);
    }

    @Override
    public Tracer.SpanBuilder asChildOf(io.opentracing.Span parent) {
        if (parent == null) {
            return this;
        }
        return asChildOf(parent.context());
    }

    @Override
    public Tracer.SpanBuilder addReference(String type, io.opentracing.SpanContext referredTo) {
        if (referredTo != null && (CHILD_OF.equals(type) || FOLLOWS_FROM.equals(type))) {
            parent = (SpanContext) referredTo;
            Reference.Builder refBuilder = Reference.newBuilder();
            refBuilder.setSpanContext(parent.getInnerSpanCtx());
            if (CHILD_OF.equals(type)) {
                refBuilder.setRelationship(Relationship.CHILD_OF);
            } else {
                refBuilder.setRelationship(Relationship.FOLLOWS_FROM);
            }
            grpcSpan.addReferences(refBuilder);
        }
        return this;
    }

    @Override
    public Tracer.SpanBuilder ignoreActiveSpan() {
        ignoringActiveSpan = true;
        return this;
    }

    public Tracer.SpanBuilder withTag(String key, String value) {
        stringTags.put(key, value);
        return this;
    }

    public Tracer.SpanBuilder withTag(String key, boolean value) {
        boolTags.put(key, value);
        return this;
    }

    public Tracer.SpanBuilder withTag(String key, Number value) {
        numTags.put(key, value);
        return this;
    }

    public <T> Tracer.SpanBuilder withTag(io.opentracing.tag.Tag<T> tag, T value) {
        if (tag == null || value == null) {
            tracer.debug("tag (" + tag + ") or value (" + value + ") is null, ignoring");
            return this;
        }

        if (value instanceof Number) {
            numTags.put(tag.getKey(), (Number)value);
        } else if (value instanceof Boolean) {
            boolTags.put(tag.getKey(), (Boolean)value);
        } else {
            stringTags.put(tag.getKey(), value.toString());
        }

        return this;
    }

    public Tracer.SpanBuilder withStartTimestamp(long microseconds) {
        startTimestampMicros = microseconds;
        return this;
    }

    /**
     * Sets the traceId and the spanId for the span being created. If the span has a parent, the
     * traceId of the parent will override this traceId value.
     */
    @SuppressWarnings({"WeakerAccess", "UnusedReturnValue", "SameParameterValue"})
    public Tracer.SpanBuilder withTraceIdAndSpanId(long traceId, long spanId) {
        this.traceId = traceId;
        this.spanId = spanId;
        return this;
    }

    @SuppressWarnings("WeakerAccess")
    public Iterable<Map.Entry<String, String>> baggageItems() {
        if (parent == null) {
            return Collections.emptySet();
        } else {
            return parent.baggageItems();
        }
    }

    private SpanContext activeSpanContext() {
        io.opentracing.Span span = this.tracer.activeSpan();
        if (span == null) {
            return null;
        }

        io.opentracing.SpanContext spanContext = span.context();
        if(spanContext instanceof SpanContext) {
            return (SpanContext) spanContext;
        }

        return null;
    }

    @Override
    public io.opentracing.Span start() {
        if (tracer.isDisabled()) {
            return NoopSpan.INSTANCE;
        }

        long startTimestampRelativeNanos = -1;
        if (startTimestampMicros == 0) {
            startTimestampRelativeNanos = System.nanoTime();
            startTimestampMicros = Util.nowMicrosApproximate();
        }

        grpcSpan.setOperationName(operationName);
        grpcSpan.setStartTimestamp(Util.epochTimeMicrosToProtoTime(startTimestampMicros));

        Long traceId = this.traceId;
        Map<String, String> baggage = null;

        if(parent == null && !ignoringActiveSpan) {
            parent = activeSpanContext();
            this.asChildOf(parent);
        }

        if (parent != null) {
            traceId = parent.getTraceId();
            baggage = new HashMap<String, String>(parent.getBaggage());
        }
        SpanContext newSpanContext = new SpanContext(traceId, spanId, baggage);

        // Set the SpanContext of the span
        grpcSpan.setSpanContext(newSpanContext.getInnerSpanCtx());

        Span span = new Span(tracer, newSpanContext, grpcSpan, startTimestampRelativeNanos);
        for (Map.Entry<String, String> pair : stringTags.entrySet()) {
            span.setTag(pair.getKey(), pair.getValue());
        }
        for (Map.Entry<String, Boolean> pair : boolTags.entrySet()) {
            span.setTag(pair.getKey(), pair.getValue());
        }
        for (Map.Entry<String, Number> pair : numTags.entrySet()) {
            span.setTag(pair.getKey(), pair.getValue());
        }
        return span;
    }
}
