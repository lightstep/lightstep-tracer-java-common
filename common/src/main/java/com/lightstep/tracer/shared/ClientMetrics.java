package com.lightstep.tracer.shared;

import com.lightstep.tracer.grpc.InternalMetrics;
import com.lightstep.tracer.grpc.MetricsSample;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tracks client metrics for internal purposes.
 */
class ClientMetrics {
    private final AtomicInteger spansDropped = new AtomicInteger(0);

    void addSpansDropped(int size) {
        if (size != 0) {
            spansDropped.addAndGet(size);
        }
    }

    int getSpansDropped() {
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

    private int getAndResetSpansDropped() {
        return spansDropped.getAndSet(0);
    }
}
