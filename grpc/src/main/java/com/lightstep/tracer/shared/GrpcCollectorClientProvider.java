package com.lightstep.tracer.shared;

import io.grpc.ManagedChannelBuilder;
import io.grpc.ManagedChannelProvider;

import java.net.URL;

// public for reflective instantiation.
public class GrpcCollectorClientProvider extends CollectorClientProvider {
    private static GrpcCollectorClientProvider INSTANCE = new GrpcCollectorClientProvider();

    public static GrpcCollectorClientProvider provider() {
        return INSTANCE;
    }

    // public for reflective instantiation.
    public GrpcCollectorClientProvider() {
        ManagedChannelProvider.provider();
    }

    @Override
    protected int priority() {
        return 1;
    }

    @Override
    GrpcCollectorClient forOptions(
            AbstractTracer tracer,
            Options options
    ) {
        try {
            return new GrpcCollectorClient(
                    tracer,
                    ManagedChannelBuilder.forAddress(
                            options.collectorUrl.getHost(),
                            options.collectorUrl.getPort()
                    ).usePlaintext(options.collectorUrl.getProtocol().equals("http")),
                    options.deadlineMillis
            );
        } catch (ManagedChannelProvider.ProviderNotFoundException e) {
            // TODO - let the user know that they need to include a grpc channel dependency.
            return null;
        }
    }
}
