package com.lightstep.tracer.shared;

import io.opentracing.propagation.Binary;
import io.opentracing.propagation.TextMap;

public interface Propagator {
    <C> void inject(SpanContext spanContext, C carrier);
    <C> SpanContext extract(C carrier);

    Propagator TEXT_MAP = new TextMapPropagator();
    Propagator HTTP_HEADERS = new HttpHeadersPropagator();
    Propagator BINARY = new BinaryPropagator();
}
