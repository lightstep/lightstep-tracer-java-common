package com.lightstep.tracer.shared;

import io.grpc.ManagedChannelBuilder;
import io.grpc.ManagedChannelProvider;
import io.grpc.util.RoundRobinLoadBalancerFactory;

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
            ManagedChannelBuilder<?> builder;
            if (options.grpcCollectorTarget != null) {
                builder = ManagedChannelBuilder.forTarget(options.grpcCollectorTarget);
            } else {
                builder = ManagedChannelBuilder.forAddress(
                    options.collectorUrl.getHost(),
                    options.collectorUrl.getPort()
                );
            }

            if (options.grpcRoundRobin) {
                builder.loadBalancerFactory(RoundRobinLoadBalancerFactory.getInstance());
            }

            if (options.collectorUrl.getProtocol().equals("http")) {
                builder.usePlaintext(true);
            }
            return new GrpcCollectorClient(
                    tracer,
                    builder,
                    options.deadlineMillis
            );
        } catch (ManagedChannelProvider.ProviderNotFoundException e) {
            // TODO - let the user know that they need to include a grpc channel dependency.
            return null;
        }
    }
}
