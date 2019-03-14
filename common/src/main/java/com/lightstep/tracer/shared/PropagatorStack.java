package com.lightstep.tracer.shared;


import io.opentracing.propagation.Format;

import java.util.LinkedList;
import java.util.List;

/**
 * PropagatorStack contains a list of {@link Propagator} instances,
 * while exposing a single {@link Propagator} interface. {@link Propagator}
 * instances are added through {@link pushPropagator}.
 *
 * On {@link extract}, PropagatorStack tries each {@link Propagator}, starting
 * at the top, stopping with the first {@link io.opentracing.SpanContext} that is
 * successfully extracted.
 *
 * On {@link inject}, PropagatorStack inserts all propagation formats into
 * the carrier.
 */
public final class PropagatorStack implements Propagator {
    Format format;
    List<Propagator> propagators;

    /**
     * Creates a new PropagatorStack associated with the specified
     * {@link io.opentracing.propagation.Format}.
     *
     * Observe that a new {@link Format} should *not* be created if the data is being
     * propagated through http headers or a text map. Instead, use the respective
     * builtin {@link Format}. In case of doubt, contact LightStep for advice.
     *
     * @param format Instance of {@link io.opentracing.propagation.Format}
     *               associated with this PropagatorStack.
     */
    public PropagatorStack(Format format) {
        if (format == null) {
            throw new IllegalArgumentException("format cannot be null");
        }

        this.format = format;
        propagators = new LinkedList<Propagator>();
    }

    public Format format() {
        return format;
    }

    /**
     * Pushes a {@link Propagator} onto the top of the stack.
     *
     * @param propagator Instance of {@link Propagator} used as part of extraction and injection.
     */
    public PropagatorStack pushPropagator(Propagator propagator) {
        if (propagator == null) {
            throw new IllegalArgumentException("propagator cannot be null");
        }

        propagators.add(propagator);
        return this;
    }

    public <C> SpanContext extract(C carrier) {
        for (int i = propagators.size() - 1; i >= 0; i--) {
            SpanContext context = propagators.get(i).extract(carrier);
            if (context != null)
                return context;
        }

        return null;
    }

    public <C> void inject(SpanContext context, C carrier) {
        for (int i = 0; i < propagators.size(); i++) {
            propagators.get(i).inject(context, carrier);
        }
    }
}
