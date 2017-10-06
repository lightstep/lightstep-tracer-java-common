package com.lightstep.tracer.shared;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * N.B. The pattern of loading the client class dynamically based on imports
 * was borrowed from how GRPC loads it's transport layer.
 * They also made a distinction about how to load classes in and out of
 * an Android environment. We copied their pattern.
 *
 * https://github.com/grpc/grpc-java/blob/v1.6.1/core/src/main/java/io/grpc/ManagedChannelProvider.java
 */
public abstract class CollectorClientProvider {
    private static final CollectorClientProvider provider = load();

    public static class ProviderNotFoundException extends RuntimeException {
        public ProviderNotFoundException(String message) {
            super(message);
        }
    }

    public static CollectorClientProvider provider() throws ProviderNotFoundException {
        if (provider == null) {
            throw new ProviderNotFoundException(
                    "No functional collector client provider found. " +
                            "Try adding a dependency on the tracer-okhttp or tracer-grpc artifact");
        }
        return provider;
    }

    private static CollectorClientProvider load() {
        Iterable<CollectorClientProvider> candidates = loadCandidates();

        CollectorClientProvider candidate = null;

        // Find the first highest priority provider.
        for (CollectorClientProvider current : candidates) {
            if (candidate == null || current.priority() > candidate.priority()) {
                candidate = current;
            }
        }

        return candidate;
    }

    private static ClassLoader getCorrectClassLoader() {
        if (isAndroid()) {
            // When android:sharedUserId or android:process is used, Android will setup a dummy
            // ClassLoader for the thread context (http://stackoverflow.com/questions/13407006),
            // instead of letting users to manually set context class loader, we choose the
            // correct class loader here.
            return CollectorClientProvider.class.getClassLoader();
        }
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * Returns whether current platform is Android.
     */
    private static boolean isAndroid() {
        try {
            Class.forName("android.app.Application", /*initialize=*/ false, null);
            return true;
        } catch (Exception e) {
            // If Application isn't loaded, it might as well not be Android.
            return false;
        }
    }

    private static Iterable<CollectorClientProvider> loadCandidates() {
        if (isAndroid()) {
            return getCandidatesViaHardCoded(getCorrectClassLoader());
        }
        return getCandidatesViaServiceLoader();
    }

    private static Collection<CollectorClientProvider> getCandidatesViaHardCoded(ClassLoader loader) {
        List<CollectorClientProvider> list = new ArrayList<>(2);

        try {
            list.add(create(Class.forName(
                    "com.lightstep.tracer.shared.GrpcCollectorClientProvider",
                    true,
                    loader
            )));
        } catch (ClassNotFoundException ex) {
            // ignore
        }

        try {
            list.add(create(Class.forName(
                    "com.lightstep.tracer.shared.HttpCollectorClientProvider",
                    true,
                    loader
            )));
        } catch (ClassNotFoundException ex) {
            // ignore
        }

        return list;
    }

    private static Iterable<CollectorClientProvider> getCandidatesViaServiceLoader() {
        return ServiceLoader.load(CollectorClientProvider.class);
    }

    private static CollectorClientProvider create(Class<?> rawClass) {
        try {
            return rawClass.asSubclass(CollectorClientProvider.class).getConstructor().newInstance();
        } catch (Throwable t) {
            throw new ServiceConfigurationError(
                    "Provider " + rawClass.getName() + " could not be instantiated: " + t,
                    t
            );
        }
    }

    protected abstract int priority();

    abstract CollectorClient forUrl(
            AbstractTracer tracer,
            URL collectorURL,
            long deadlineMillis,
            ClientMetrics clientMetrics
    );
}
