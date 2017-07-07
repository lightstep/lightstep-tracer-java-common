package com.lightstep.tracer.shared;

import com.lightstep.tracer.grpc.InternalMetrics;
import com.lightstep.tracer.grpc.MetricsSample;

import java.util.ArrayList;

/**
 * Tracks client metrics for internal purposes.
 */
class ClientMetrics {

    /**
     * For capacity allocation purposes, keep this in sync with the number of counts actually being
     * tracked.
     */
    private static final int NUMBER_OF_COUNTS = 1;
    long spansDropped;

    ClientMetrics() {
        spansDropped = 0;
    }

    InternalMetrics toGrpc() {
        ArrayList<MetricsSample> counts = new ArrayList<>(NUMBER_OF_COUNTS);
        counts.add(MetricsSample.newBuilder().setName("spans.dropped")
            .setIntValue(spansDropped).build());
        return InternalMetrics.newBuilder().addAllCounts(counts).build();

    }
}
