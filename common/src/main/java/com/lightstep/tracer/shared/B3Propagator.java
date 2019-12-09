package com.lightstep.tracer.shared;

import io.opentracing.propagation.TextMapInject;
import io.opentracing.propagation.TextMapExtract;
import java.util.Map;

public class B3Propagator implements Propagator {

    private static final String TRACE_ID_NAME = "X-B3-TraceId";
    private static final String SPAN_ID_NAME = "X-B3-SpanId";
    private static final String SAMPLED_NAME = "X-B3-Sampled";

    @Override
    public <C> void inject(SpanContext spanContext, C carrier) {
        if (!(carrier instanceof TextMapInject)) {
            return;
        }

        TextMapInject textCarrier = (TextMapInject) carrier;
        long traceId = spanContext.getTraceId();
        long spanId = spanContext.getSpanId();
        String foreignTraceId = spanContext.getForeignTraceId();

        // Inject the original, non-truncated traceId if available.
        if (foreignTraceId != null) {
            textCarrier.put(TRACE_ID_NAME, foreignTraceId);
        } else {
            textCarrier.put(TRACE_ID_NAME, Util.toHexString(traceId));
        }
        textCarrier.put(SPAN_ID_NAME, Util.toHexString(spanId));
        textCarrier.put(SAMPLED_NAME, "1");
    }

    @Override
    public <C> SpanContext extract(C carrier) {
        if (!(carrier instanceof TextMapExtract)) {
            return null;
        }

        Long traceId = null;
        Long spanId = null;
        String foreignTraceId = null;

        for (Map.Entry<String, String> entry : (TextMapExtract)carrier) {
            if (entry.getKey().equalsIgnoreCase(TRACE_ID_NAME)) {
                foreignTraceId = entry.getValue();
                traceId = Util.fromHexString(normalizeTraceId(foreignTraceId));
            } else if (entry.getKey().equalsIgnoreCase(SPAN_ID_NAME)) {
                spanId = Util.fromHexString(entry.getValue());
            }
        }

        if (null != traceId && null != spanId) {
            return new SpanContext(traceId, spanId, foreignTraceId);
        }

        return null;
    }

    // Use the 64 least significant bits, represented by the right-most
    // 16 characters. See https://github.com/openzipkin/b3-propagation#traceid-1
    static String normalizeTraceId(String traceId) {
        if (traceId.length() > 16) {
            return traceId.substring(traceId.length() - 16);
        }
        return traceId;
    }
}

