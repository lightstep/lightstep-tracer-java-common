package com.lightstep.tracer.shared;

import com.lightstep.tracer.grpc.InternalMetrics;
import com.lightstep.tracer.grpc.MetricsSample;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks client metrics for internal purposes.
 */
class ClientMetrics {
    private final AtomicLong spansDropped = new AtomicLong(0);

    void addSpansDropped(int size) {
        spansDropped.addAndGet(size);
    }

    long getSpansDropped() {
        return spansDropped.get();
    }

    InternalMetrics toInternalMetricsAndReset() {
        return InternalMetrics.newBuilder()
                .addCounts(MetricsSample.newBuilder()
                        .setName("spans.dropped")
                        .setIntValue(getAndResetSpansDropped())
                        .build()
                ).build();
    }

    private long getAndResetSpansDropped() {
        return spansDropped.getAndSet(0);
    }
}
