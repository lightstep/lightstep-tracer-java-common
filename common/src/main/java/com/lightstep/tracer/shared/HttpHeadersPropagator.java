package com.lightstep.tracer.shared;

import io.opentracing.propagation.TextMap;

/**
 * TODO: HTTP_HEADERS presently blindly delegates to TextMapPropagator; adopt BasicTracer's HTTP
 * carrier encoding once it's been defined.
 */
class HttpHeadersPropagator implements Propagator<TextMap> {
    public void inject(SpanContext spanContext, TextMap carrier, boolean useB3Headers) {
        TEXT_MAP.inject(spanContext, carrier, useB3Headers);
    }

    public SpanContext extract(TextMap carrier, boolean useB3Headers) {
        return TEXT_MAP.extract(carrier, useB3Headers);
    }
}