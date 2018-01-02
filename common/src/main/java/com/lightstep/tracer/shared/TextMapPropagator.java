package com.lightstep.tracer.shared;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.opentracing.propagation.TextMap;

class TextMapPropagator implements Propagator<TextMap> {
    private static final Locale english = new Locale("en", "US");

    private static final String PREFIX_TRACER_STATE = "ot-tracer-";
    static final String PREFIX_BAGGAGE = "ot-baggage-";

    static final String FIELD_NAME_TRACE_ID = PREFIX_TRACER_STATE + "traceid";
    static final String FIELD_NAME_SPAN_ID = PREFIX_TRACER_STATE + "spanid";
    static final String FIELD_NAME_SAMPLED = PREFIX_TRACER_STATE + "sampled";

    public void inject(SpanContext spanContext, final TextMap carrier) {
        carrier.put(FIELD_NAME_TRACE_ID, Util.toHexString(spanContext.getTraceId()));
        carrier.put(FIELD_NAME_SPAN_ID, Util.toHexString(spanContext.getSpanId()));
        carrier.put(FIELD_NAME_SAMPLED, "true");
        for (Map.Entry<String, String> e : spanContext.baggageItems()) {
            carrier.put(PREFIX_BAGGAGE + e.getKey(), e.getValue());
        }
    }

    public SpanContext extract(TextMap carrier) {
        Long traceId = null;
        Long spanId = null;
        Map<String, String> baggage = new HashMap<>();

        for (Map.Entry<String, String> entry : carrier) {
            String key = entry.getKey().toLowerCase(english);

            if (FIELD_NAME_TRACE_ID.equals(key)) {
                traceId = Util.fromHexString(entry.getValue());
            }

            if (FIELD_NAME_SPAN_ID.equals(key)) {
                spanId = Util.fromHexString(entry.getValue());
            }

            if (key.startsWith(PREFIX_BAGGAGE)) {
                baggage.put(key.substring(PREFIX_BAGGAGE.length()), entry.getValue());
            }
        }

        if (traceId == null || spanId == null) {
            return null;
        }

        // Success.
        return new SpanContext(traceId, spanId, baggage);
    }
}
