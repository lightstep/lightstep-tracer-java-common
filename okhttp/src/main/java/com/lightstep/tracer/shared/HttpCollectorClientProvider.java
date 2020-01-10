package com.lightstep.tracer.shared;

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
    protected Options.ClientProvider type() {
        return Options.ClientProvider.HTTP;
    }

    @Override
    HttpCollectorClient forOptions(
            AbstractTracer tracer,
            Options options
    ) {
        return new HttpCollectorClient(
                tracer,
                options.collectorUrl,
                options.deadlineMillis,
                options.okhttpDns
        );
    }
}
