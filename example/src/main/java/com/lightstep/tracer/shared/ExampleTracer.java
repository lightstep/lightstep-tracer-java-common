package com.lightstep.tracer.shared;

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
    protected void printLogToConsole(InternalLogLevel level, String msg, Throwable throwable) {
        System.out.println(msg);
        if (throwable != null) {
            throwable.printStackTrace();
        }
    }
}
