package com.lightstep.tracer.shared;

import io.opentracing.propagation.TextMap;
import java.util.Map;

public class B3Propagator implements Propagator<TextMap> {

    private static final String TRACE_ID_NAME = "X-B3-TraceId";
    private static final String SPAN_ID_NAME = "X-B3-SpanId";
    private static final String SAMPLED_NAME = "X-B3-Sampled";

    private final TextMapPropagator textMapPropagator = new TextMapPropagator();

    @Override
    public void inject(SpanContext spanContext, TextMap carrier) {
        long traceId = spanContext.getTraceId();
        long spanId = spanContext.getSpanId();

        carrier.put(TRACE_ID_NAME, Util.toHexString(traceId));
        carrier.put(SPAN_ID_NAME, Util.toHexString(spanId));
        carrier.put(SAMPLED_NAME, "true");

        // Default to use TextMap propagation in case we could not find TraceId
        if (0L == traceId) {
            textMapPropagator.inject(spanContext, carrier);
        }
    }

    @Override
    public SpanContext extract(TextMap carrier) {
        Long traceId = null;
        Long spanId = null;

        for (Map.Entry<String, String> entry : carrier) {
            if (entry.getKey().equalsIgnoreCase(TRACE_ID_NAME)) {
                traceId = Util.fromHexString(entry.getValue());
            } else if (entry.getKey().equalsIgnoreCase(SPAN_ID_NAME)) {
                spanId = Util.fromHexString(entry.getValue());
            }
        }

        if (null != traceId && null != spanId) {
            return new SpanContext(traceId, spanId);
        } else {
            // Default to use TextMap propagation
            return textMapPropagator.extract(carrier);
        }
    }
}

