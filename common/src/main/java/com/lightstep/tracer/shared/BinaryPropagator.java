package com.lightstep.tracer.shared;

import io.opentracing.propagation.Binary;

class BinaryPropagator implements Propagator {
    public <C> void inject(SpanContext spanContext, C carrier) {
        // TODO: implement
    }

    public <C> SpanContext extract(C carrier) {
        // TODO: implement
        return null;
    }
}
