package com.lightstep.tracer.shared;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic implementation of AbstractTracer for use in unit testing.
 */
class StubTracer extends AbstractTracer {
    private final List<LogCall> consoleLogCalls;
    SimpleFuture<Boolean> flushResult = null;

    private static final SimpleFuture<Boolean> SUCCESS_FUTURE = new SimpleFuture<>(true);

    StubTracer(Options options) {
        super(options);
        consoleLogCalls = new ArrayList<>();
    }

    @Override
    protected SimpleFuture<Boolean> flushInternal(boolean explicitRequest) {
        if (flushResult == null) {
            return SUCCESS_FUTURE;
        }

        return flushResult;
    }

    @Override
    protected void printLogToConsole(InternalLogLevel level, String msg, Throwable throwable) {
        // this can only be null when print is called during Tracer construction
        if (consoleLogCalls != null) {
            consoleLogCalls.add(new LogCall(level, msg, throwable));
        }
    }

    boolean consoleLogCallsIsEmpty() {
        return consoleLogCalls.isEmpty();
    }

    int getNumberOfConsoleLogCalls() {
        if (consoleLogCallsIsEmpty()) {
            return 0;
        }
        return consoleLogCalls.size();
    }

    static class LogCall {
        final InternalLogLevel level;
        final String msg;
        final Throwable throwable;

        private LogCall(InternalLogLevel level, String msg, Throwable throwable)  {
            this.level = level;
            this.msg = msg;
            this.throwable = throwable;
        }
    }
}
