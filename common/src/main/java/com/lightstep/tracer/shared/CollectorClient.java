package com.lightstep.tracer.shared;

import com.lightstep.tracer.grpc.ReportRequest;
import com.lightstep.tracer.grpc.ReportResponse;

// Has to be an abstract class and not an interface becuase interfaces don't allow package private methods.
abstract class CollectorClient {
    abstract void reconnect();

    abstract void shutdown();

    abstract ReportResponse report(ReportRequest request);
}
