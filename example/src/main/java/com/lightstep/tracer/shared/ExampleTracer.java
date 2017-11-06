package com.lightstep.tracer.shared;

import com.lightstep.tracer.shared.AbstractTracer;
import com.lightstep.tracer.shared.Options;
import com.lightstep.tracer.shared.SimpleFuture;
import io.opentracing.Span;

public class ExampleTracer extends AbstractTracer {
    public ExampleTracer(Options options) {
        super(options);
    }

    @Override
    // Flush any data stored in the log and span buffers
    protected SimpleFuture<Boolean> flushInternal(boolean explicitRequest) {
        return new SimpleFuture<>(sendReport(explicitRequest));
    }

    @Override
    protected void printLogToConsole(InternalLogLevel level, String msg, Object payload) {
        String s = msg;
        if (payload != null) {
            s += " " + payload.toString();
        }
        System.out.println(s);
    }
}
