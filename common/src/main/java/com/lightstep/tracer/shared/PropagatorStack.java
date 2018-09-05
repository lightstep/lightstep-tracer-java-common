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
public final class PropagatorStack<C> implements Propagator<C> {
    Format<C> format;
    List<Propagator<C>> propagators;

    /**
     * Creates a new PropagatorStack associated witht he specified
     * {@link io.opentracing.propagation.Format}. It is an error
     * to specify a format that is contained in
     * {link io.opentracing.propagation.Format#Builtin}.
     *
     * @param format Instance of {@link io.opentracing.propagation.Format}
     *               associated with this PropagatorStack.
     */
    public PropagatorStack(Format<C> format) {
        if (format == null) {
            throw new IllegalArgumentException("format cannot be null");
        }
        if (!isValidFormat(format)) {
            throw new IllegalArgumentException("format not recognized");
        }

        this.format = format;
        propagators = new LinkedList<Propagator<C>>();
    }

    static boolean isValidFormat(Format format) {
        return format == Format.Builtin.TEXT_MAP
            || format == Format.Builtin.HTTP_HEADERS
            || format == Format.Builtin.BINARY;
    }

    public Format<C> format() {
        return format;
    }

    /**
     * Pushes a {@link Propagator} onto the top of the stack.
     *
     * @param propagator Instance of {@link Propagator} used as part of extraction and injection.
     */
    public PropagatorStack pushPropagator(Propagator<C> propagator) {
        if (propagator == null) {
            throw new IllegalArgumentException("propagator cannot be null");
        }

        propagators.add(propagator);
        return this;
    }

    public SpanContext extract(C carrier) {
        for (int i = propagators.size() - 1; i >= 0; i--) {
            SpanContext context = propagators.get(i).extract(carrier);
            if (context != null)
                return context;
        }

        return null;
    }

    public void inject(SpanContext context, C carrier) {
        for (int i = 0; i < propagators.size(); i++) {
            propagators.get(i).inject(context, carrier);
        }
    }
}
