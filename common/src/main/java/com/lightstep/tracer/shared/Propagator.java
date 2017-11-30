package com.lightstep.tracer.shared;

import java.nio.ByteBuffer;

import io.opentracing.propagation.TextMap;

interface Propagator<C> {
    void inject(SpanContext spanContext, C carrier, boolean useB3Headers);

    SpanContext extract(C carrier, boolean useB3Headers);

    Propagator<TextMap> TEXT_MAP = new TextMapPropagator();
    Propagator<TextMap> HTTP_HEADERS = new HttpHeadersPropagator();
    Propagator<ByteBuffer> BINARY = new BinaryPropagator();
}