package com.lightstep.tracer.shared;

import io.opentracing.propagation.Binary;

class BinaryPropagator implements Propagator<Binary> {
    public void inject(SpanContext spanContext, Binary carrier) {
        // TODO: implement
    }

    public SpanContext extract(Binary carrier) {
        // TODO: implement
        return null;
    }
}
