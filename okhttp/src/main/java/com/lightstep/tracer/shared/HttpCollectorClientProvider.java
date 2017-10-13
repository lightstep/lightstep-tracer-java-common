package com.lightstep.tracer.shared;

import java.net.URL;

// public for reflective instantiation.
public class HttpCollectorClientProvider extends CollectorClientProvider {
    private static HttpCollectorClientProvider INSTANCE = new HttpCollectorClientProvider();

    public static HttpCollectorClientProvider provider() {
        return INSTANCE;
    }

    // public for reflective instantiation.
    public HttpCollectorClientProvider() {}

    @Override
    protected int priority() {
        return 0;
    }

    @Override
    public HttpCollectorClient forUrl(
            AbstractTracer tracer,
            URL collectorURL,
            long deadlineMillis,
            ClientMetrics clientMetrics
    ) {
        return new HttpCollectorClient();
    }
}
