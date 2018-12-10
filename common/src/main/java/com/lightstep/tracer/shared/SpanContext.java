package com.lightstep.tracer.shared;

import java.util.HashMap;
import java.util.Map;

public class SpanContext implements io.opentracing.SpanContext {
    private final long traceId;
    private final long spanId;
    private final Map<String, String> baggage;
    private final String[] toIds = new String[]{"", ""};

    public SpanContext() {
        this(Util.generateRandomGUID(), Util.generateRandomGUID());
    }

    public SpanContext(long traceId, long spanId) {
        this(traceId, spanId, null);
    }

    SpanContext(long traceId) {
        this(traceId, Util.generateRandomGUID());
    }

    SpanContext(long traceId, Map<String, String> baggage) {
        this(traceId, Util.generateRandomGUID(), baggage);
    }

    SpanContext(Long traceId, Long spanId, Map<String, String> baggage) {
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
        toIds[0] = Util.toHexString(traceId);
        toIds[1] = Util.toHexString(spanId);
    }

    @SuppressWarnings("WeakerAccess")
    public long getSpanId() {
        return this.spanId;
    }

    @SuppressWarnings("WeakerAccess")
    public long getTraceId() {
        return this.traceId;
    }

    String getBaggageItem(String key) {
        return this.baggage.get(key);
    }

    Map<String, String> getBaggage() {
        return baggage;
    }

    SpanContext withBaggageItem(String key, String value) {
        // This is really a "set" not a "with" but keeping as is to preserve behavior.
        this.baggage.put(key, value);
        return new SpanContext(this.getTraceId(), this.getSpanId(), this.baggage);
    }

    @Override
    public Iterable<Map.Entry<String, String>> baggageItems() {
        return this.baggage.entrySet();
    }

    @Override
    public String toTraceId() {
        return toIds[0];
    }

    @Override
    public String toSpanId() {
        return toIds[1];
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
