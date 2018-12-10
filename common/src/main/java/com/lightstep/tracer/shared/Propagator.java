package com.lightstep.tracer.shared;

import io.opentracing.propagation.Binary;
import io.opentracing.propagation.TextMap;

public interface Propagator<C> {
    void inject(SpanContext spanContext, C carrier);

    SpanContext extract(C carrier);

    Propagator<TextMap> TEXT_MAP = new TextMapPropagator();
    Propagator<TextMap> HTTP_HEADERS = new HttpHeadersPropagator();
    Propagator<Binary> BINARY = new BinaryPropagator();
}
