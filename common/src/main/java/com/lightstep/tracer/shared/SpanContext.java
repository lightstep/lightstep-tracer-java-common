package com.lightstep.tracer.shared;

import java.util.HashMap;
import java.util.Map;

public class SpanContext implements io.opentracing.SpanContext {
    private final long traceId;
    private final long spanId;
    private final Map<String, String> baggage;
    private final String foreignTraceId;
    private final String[] toIds = new String[]{"", ""};
    private static final int TRACE_INDEX = 0;
    private static final int SPAN_INDEX = 1;

    public SpanContext() {
        this(Util.generateRandomGUID(), Util.generateRandomGUID());
    }

    public SpanContext(long traceId, long spanId) {
        this(traceId, spanId, null, null);
    }

    SpanContext(long traceId) {
        this(traceId, Util.generateRandomGUID());
    }

    SpanContext(long traceId, Map<String, String> baggage) {
        this(traceId, Util.generateRandomGUID(), baggage);
    }

    SpanContext(Long traceId, Long spanId, Map<String, String> baggage) {
        this(traceId, spanId, baggage, null);
    }

    SpanContext(Long traceId, Long spanId, String foreignTraceId) {
        this(traceId, spanId, null, foreignTraceId);
    }

    SpanContext(Long traceId, Long spanId, Map<String, String> baggage, String foreignTraceId) {
        if (traceId == null) {
            traceId = Util.generateRandomGUID();
        }

        if (spanId == null) {
            spanId = Util.generateRandomGUID();
        }

        if (baggage == null) {
            baggage = new HashMap<>();
        }

        this.traceId = traceId;
        this.spanId = spanId;
        this.baggage = baggage;
        this.foreignTraceId = foreignTraceId;
        toIds[TRACE_INDEX] = Util.toHexString(traceId);
        toIds[SPAN_INDEX] = Util.toHexString(spanId);
    }

    @SuppressWarnings("WeakerAccess")
    public long getSpanId() {
        return this.spanId;
    }

    @SuppressWarnings("WeakerAccess")
    public long getTraceId() {
        return this.traceId;
    }

    public String getForeignTraceId() {
        return this.foreignTraceId;
    }

    @Override
    public String toTraceId() {
        return toIds[TRACE_INDEX];
    }

    @Override
    public String toSpanId() {
        return toIds[SPAN_INDEX];
    }

    String getBaggageItem(String key) {
        return this.baggage.get(key);
    }

    Map<String, String> getBaggage() {
        return baggage;
    }

    @SuppressWarnings("WeakerAccess")
    public SpanContext withBaggageItem(String key, String value) {
        // This is really a "set" not a "with" but keeping as is to preserve behavior.
        this.baggage.put(key, value);
        return new SpanContext(this.getTraceId(), this.getSpanId(), this.baggage, this.foreignTraceId);
    }

    @Override
    public Iterable<Map.Entry<String, String>> baggageItems() {
        return this.baggage.entrySet();
    }

    @SuppressWarnings("WeakerAccess")
    public com.lightstep.tracer.grpc.SpanContext getInnerSpanCtx() {
        return com.lightstep.tracer.grpc.SpanContext.newBuilder()
                .setTraceId(this.getTraceId())
                .setSpanId(this.getSpanId())
                .putAllBaggage(this.baggage)
                .build();
    }
}
