package com.lightstep.tracer.shared;

import io.opentracing.propagation.TextMap;

/**
 * TODO: HTTP_HEADERS presently blindly delegates to TextMapPropagator; adopt BasicTracer's HTTP
 * carrier encoding once it's been defined.
 */
class HttpHeadersPropagator implements Propagator {
    public <C> void inject(SpanContext spanContext, C carrier) {
        TEXT_MAP.inject(spanContext, carrier);
    }

    public <C> SpanContext extract(C carrier) {
        return TEXT_MAP.extract(carrier);
    }
}
