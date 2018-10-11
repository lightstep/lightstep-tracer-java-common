package com.lightstep.tracer.shared;


import io.opentracing.ScopeManager;
import io.opentracing.SpanContext;
import io.opentracing.propagation.Format;
import io.opentracing.util.ThreadLocalScopeManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Options control behaviors specific to the LightStep tracer.
 */
public final class Options {

    // DEFAULT OPTIONS

    /**
     * Java System property that will be used as the component name when no other value is provided.
     */
    private static final String COMPONENT_NAME_SYSTEM_PROPERTY_KEY = "sun.java.command";

    /**
     * Hostname that will be used for the collector if no other value is provided.
     */
    private static final String DEFAULT_COLLECTOR_HOST = "collector-grpc.lightstep.com";

    /**
     * Default collector port for HTTPS
     */
    static final int DEFAULT_SECURE_PORT = 443;

    /**
     * Default collector port for HTTP
     */
    static final int DEFAULT_PLAINTEXT_PORT = 80;

    /**
     * Default maximum number of Spans buffered locally (a protective mechanism)
     */
    @SuppressWarnings("WeakerAccess")
    public static final int DEFAULT_MAX_BUFFERED_SPANS = 1000;

    /**
     * Default interval at which spans will be flushed
     */
    private static final long DEFAULT_REPORTING_INTERVAL_MILLIS = 3000;

    /**
     * Default duration the tracer should wait for a response from the collector when sending a report.
     */
    private static final long DEFAULT_DEADLINE_MILLIS = 30000;

    static final String HTTPS = "https";

    static final String HTTP = "http";

    static final String COLLECTOR_PATH = "/api/v2/reports";

    // TAG KEYS

    static final String LEGACY_COMPONENT_NAME_KEY = "component_name";

    static final String COMPONENT_NAME_KEY = "lightstep.component_name";

    static final String GUID_KEY = "lightstep.guid";

    // BUILTIN PROPAGATORS
    static final Map<Format<?>, Propagator<?>> BUILTIN_PROPAGATORS = Collections.unmodifiableMap(
            new HashMap<Format<?>, Propagator<?>>() {{
                put(Format.Builtin.TEXT_MAP, Propagator.TEXT_MAP);
                put(Format.Builtin.HTTP_HEADERS, Propagator.HTTP_HEADERS);
                put(Format.Builtin.BINARY, Propagator.BINARY);
            }}
    );

    // LOG LEVELS

    /**
     * all internal log statements, including debugging details
     */
    @SuppressWarnings("WeakerAccess")
    public static final int VERBOSITY_DEBUG = 4;

    /**
     * all errors, warnings, and info statements are echoed locally
     */
    @SuppressWarnings("WeakerAccess")
    public static final int VERBOSITY_INFO = 3;

    /**
     * all errors are echoed locally
     */
    @SuppressWarnings("WeakerAccess")
    public static final int VERBOSITY_ERRORS_ONLY = 2;

    /**
     * only the first error encountered will be echoed locally
     */
    @SuppressWarnings("WeakerAccess")
    public static final int VERBOSITY_FIRST_ERROR_ONLY = 1;

    /**
     * never produce local output
     */
    @SuppressWarnings("WeakerAccess")
    public static final int VERBOSITY_NONE = 0;

    final String accessToken;
    final URL collectorUrl;
    final Map<String, Object> tags;
    final long maxReportingIntervalMillis;
    final int maxBufferedSpans;
    final int verbosity;
    final boolean disableReportingLoop;
    // reset GRPC client at regular intervals (for load balancing)
    final boolean resetClient;
    final boolean useClockCorrection;
    final ScopeManager scopeManager;
    final Map<Format<?>, Propagator<?>> propagators;

    /**
     * The maximum amount of time the tracer should wait for a response from the collector when sending a report.
     */
    final long deadlineMillis;

    private Options(
            String accessToken,
            URL collectorUrl,
            long maxReportingIntervalMillis,
            int maxBufferedSpans,
            int verbosity,
            boolean disableReportingLoop,
            boolean resetClient,
            Map<String, Object> tags,
            boolean useClockCorrection,
            ScopeManager scopeManager,
            long deadlineMillis,
            Map<Format<?>, Propagator<?>> propagators
    ) {
        this.accessToken = accessToken;
        this.collectorUrl = collectorUrl;
        this.maxReportingIntervalMillis = maxReportingIntervalMillis;
        this.maxBufferedSpans = maxBufferedSpans;
        this.verbosity = verbosity;
        this.disableReportingLoop = disableReportingLoop;
        this.resetClient = resetClient;
        this.tags = tags;
        this.useClockCorrection = useClockCorrection;
        this.scopeManager = scopeManager;
        this.deadlineMillis = deadlineMillis;
        this.propagators = propagators;
    }

    long getGuid() {
        return (long) tags.get(GUID_KEY);
    }

    @SuppressWarnings({"WeakerAccess"})
    public static class OptionsBuilder {
        private String accessToken;
        private String collectorProtocol = HTTPS;
        private String collectorHost = DEFAULT_COLLECTOR_HOST;
        private int collectorPort = -1;
        private long maxReportingIntervalMillis;
        private int maxBufferedSpans = -1;
        private int verbosity = 1;
        private boolean disableReportingLoop = false;
        private boolean resetClient = true;
        private boolean useClockCorrection = true;
        private Map<String, Object> tags = new HashMap<>();
        private ScopeManager scopeManager;
        private long deadlineMillis = -1;
        private Map<Format<?>, Propagator<?>> propagators = new HashMap<>();

        public OptionsBuilder() {
        }

        public OptionsBuilder(Options options) {
            this.accessToken = options.accessToken;
            this.collectorProtocol = options.collectorUrl.getProtocol();
            this.collectorHost = options.collectorUrl.getHost();
            this.collectorPort = options.collectorUrl.getPort();
            this.maxReportingIntervalMillis = options.maxReportingIntervalMillis;
            this.maxBufferedSpans = options.maxBufferedSpans;
            this.verbosity = options.verbosity;
            this.disableReportingLoop = options.disableReportingLoop;
            this.resetClient = options.resetClient;
            this.tags = options.tags;
            this.scopeManager = options.scopeManager;
            this.useClockCorrection = options.useClockCorrection;
            this.deadlineMillis = options.deadlineMillis;
            this.propagators = options.propagators;
        }

        /**
         * Adds a user defined {@link Propagator} to be used during
         * {@link io.opentracing.Tracer#inject} and {@link io.opentracing.Tracer#extract} for
         * the given type. This can be used to provide custom handling for a specified
         * {@link Format} (such as {@link Format.Builtin#TEXT_MAP}).
         *
         * {@link Propagator#inject} and {@link Propagator#extract} are expected
         * to fail silently in case of error during injection and extraction, respectively.
         *
         * Observe that a new {@link Format} should *not* be created if the data is being
         * propagated through http headers or a text map. Instead, use the respective
         * builtin {@link Format} and specify a custom {@link Propagator} here. In case of
         * doubt, contact LightStep for advice.
         *
         * @param format Instance of {@link Format} for which custom Propagator will be used.
         * @param propagator Instance of {@link Propagator} to be used
         * @param <T> Type of the carrier.
         */
        public <T> OptionsBuilder withPropagator(Format<T> format, Propagator<T> propagator) {
            if (format == null) {
                throw new IllegalArgumentException("format cannot be null");
            }
            if (propagator == null) {
                throw new IllegalArgumentException("propagator cannot be null");
            }

            this.propagators.put(format, propagator);
            return this;
        }


        /**
         * Sets the unique identifier for this application.
         *
         * @param accessToken Your specific token for LightStep access.
         */
        public OptionsBuilder withAccessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        /**
         * Sets the protocol which will be used when sending data to the tracer.
         *
         * @param protocol Either 'http' or 'https'
         * @throws IllegalArgumentException If the protocol argument is invalid.
         */
        public OptionsBuilder withCollectorProtocol(String protocol) {
            if (!HTTPS.equals(protocol) && !HTTP.equals(protocol)) {
                throw new IllegalArgumentException("Invalid protocol for collector: " + protocol);
            }
            this.collectorProtocol = protocol;
            return this;
        }

        /**
         * Sets the host to which the tracer will send data. If not set, will default to the
         * primary LightStep collector address.
         *
         * @param collectorHost The hostname for the LightStep collector.
         * @throws IllegalArgumentException If the collectorHost argument is invalid.
         */
        public OptionsBuilder withCollectorHost(String collectorHost) {
            if (collectorHost == null || "".equals(collectorHost.trim())) {
                throw new IllegalArgumentException("Invalid collector host: " + collectorHost);
            }
            this.collectorHost = collectorHost;
            return this;
        }

        /**
         * Sets the port to which the tracer will send data. If not set, will default to
         * {@code DEFAULT_SECURE_PORT} when the protocol is https and {@code DEFAULT_PLAINTEXT_PORT}
         * when the protocol is http.
         *
         * @param collectorPort The port for the LightStep collector.
         * @throws IllegalArgumentException If the collectorPort is invalid.
         */
        public OptionsBuilder withCollectorPort(int collectorPort) {
            if (collectorPort <= 0) {
                throw new IllegalArgumentException("Invalid collector port: " + collectorPort);
            }
            this.collectorPort = collectorPort;
            return this;
        }

        /**
         * Sets the component name attribute. If not set, will default to the Java runtime
         * command.
         *
         * @param name The name of the component being traced.
         */
        public OptionsBuilder withComponentName(String name) {
            return withTag(COMPONENT_NAME_KEY, name);
        }

        /**
         * Sets a user-defined key-value pair that should be associated with all of the
         * data produced by this tracer.
         */
        public OptionsBuilder withTag(String key, Object value) {
            tags.put(key, value);
            return this;
        }

        /**
         * Sets the maximum interval between reports.
         *
         * @param maxReportingIntervalMillis The maximum interval of time that will pass between
         *                                   reports.
         */
        public OptionsBuilder withMaxReportingIntervalMillis(int maxReportingIntervalMillis) {
            this.maxReportingIntervalMillis = maxReportingIntervalMillis;
            return this;
        }

        /**
         * Sets the maximum number of finished Spans buffered locally before flushing. This is a protective mechanism
         * which bounds the memory usage of the LightStep library.
         *
         * @param maxBufferedSpans The maximum number of Spans buffered locally.
         */
        public OptionsBuilder withMaxBufferedSpans(int maxBufferedSpans) {
            this.maxBufferedSpans = maxBufferedSpans;
            return this;
        }

        /**
         * Controls the amount of local output produced by the tracer.  It does not
         * affect which spans are sent to the collector.  It is useful for
         * diagnosing problems in the tracer itself. The default value is 1.
         *
         * 0 - never produce local output
         * 1 - only the first error encountered will be echoed locally
         * 2 - all errors are echoed locally
         * 3 - all errors, warnings, and info statements are echoed locally
         * 4 - all internal log statements, including debugging details
         */
        public OptionsBuilder withVerbosity(int verbosity) {
            this.verbosity = verbosity;
            return this;
        }

        /**
         * If true, the background reporting loop will be disabled. Reports will
         * only occur on explicit calls to Flush(); If not set, will default to
         * {@code DEFAULT_REPORTING_INTERVAL_MILLIS}.
         */
        @SuppressWarnings("SameParameterValue")
        public OptionsBuilder withDisableReportingLoop(boolean disable) {
            this.disableReportingLoop = disable;
            return this;
        }

        /**
         * If true, the GRPC client connection will be reset at regular intevals
         * Used to load balance on server side
         */
        public OptionsBuilder withResetClient(boolean reset) {
            this.resetClient = reset;
            return this;
        }

        /**
         * Overrides the default deadlineMillis with the provided value.
         */
        public OptionsBuilder withDeadlineMillis(long deadlineMillis) {
            this.deadlineMillis = deadlineMillis;
            return this;
        }

	@SuppressWarnings("SameParameterValue")
    public OptionsBuilder withClockSkewCorrection(boolean clockCorrection) {
	    this.useClockCorrection = clockCorrection;
	    return this;
	}

        /**
         * Sets the defaults for values not provided and constructs a new Options object.
         *
         * @return Options object configured with the built values.
         * @throws MalformedURLException If the combination of collector protocol, host, and port
         * are not valid
         */
        public Options build() throws MalformedURLException {
            defaultComponentName();
            defaultGuid();
            defaultMaxReportingIntervalMillis();
            defaultMaxBufferedSpans();
            defaultPropagators();
            defaultScopeManager();
            defaultDeadlineMillis();

            return new Options(
                    accessToken,
                    getCollectorUrl(),
                    maxReportingIntervalMillis,
                    maxBufferedSpans,
                    verbosity,
                    disableReportingLoop,
                    resetClient,
                    tags,
                    useClockCorrection,
                    scopeManager,
                    deadlineMillis,
                    propagators
            );
        }

        private void defaultScopeManager() {
            if(scopeManager == null) {
                scopeManager = new ThreadLocalScopeManager();
            }
        }

        private void defaultMaxReportingIntervalMillis() {
            if (maxReportingIntervalMillis <= 0) {
                maxReportingIntervalMillis = DEFAULT_REPORTING_INTERVAL_MILLIS;
            }
        }

        private void defaultMaxBufferedSpans() {
            if (maxBufferedSpans < 0) {
                maxBufferedSpans = DEFAULT_MAX_BUFFERED_SPANS;
            }
        }

        private void defaultGuid() {
            if (tags.get(GUID_KEY) == null) {
                withTag(GUID_KEY, Util.generateRandomGUID());
            }
        }

        /**
         * If not set, provides a default value for the component name.
         */
        private void defaultComponentName() {
            if (tags.get(COMPONENT_NAME_KEY) == null) {
                String componentNameSystemProperty = System.getProperty(COMPONENT_NAME_SYSTEM_PROPERTY_KEY);
                if (componentNameSystemProperty != null) {
                    StringTokenizer st = new StringTokenizer(componentNameSystemProperty);
                    if (st.hasMoreTokens()) {
                        String name = st.nextToken();
                        withComponentName(name);
                    }
                }
            }
        }

        private void defaultDeadlineMillis() {
            if (deadlineMillis < 0) {
                deadlineMillis = DEFAULT_DEADLINE_MILLIS;
            }
        }

        private void defaultPropagators() {
            for (Map.Entry<Format<?>, Propagator<?>> entry: BUILTIN_PROPAGATORS.entrySet()) {
                Format<?> format = entry.getKey();
                if (!propagators.containsKey(format)) {
                    propagators.put(format, entry.getValue());
                }
            }
        }

        private int getPort() {
            if (collectorPort > 0) {
                return collectorPort;
            } else if (collectorProtocol.equals(HTTPS)) {
                return DEFAULT_SECURE_PORT;
            } else {
                return DEFAULT_PLAINTEXT_PORT;
            }
        }

        private URL getCollectorUrl() throws MalformedURLException {
            int port = getPort();
            return new URL(collectorProtocol, collectorHost, port, COLLECTOR_PATH);
        }
    }

    /**
     * If this instance of Options has an overridden maxReportingIntervalMillis, returns this
     * instance of Options with that value.
     *
     * If this instance of Options is using the default {@code DEFAULT_REPORTING_INTERVAL_MILLIS}
     * then creates a new instance of Options and overrides maxReportingIntervalMillis with the
     * provided value.
     *
     * @param value A new value for maxReportingIntervalMillis. Will only be used if this Options
     *              object is current set to the default value for maxReportingIntervalMillis.
     * @throws IllegalArgumentException If this Options object has an malformed collector url.
     */
    @SuppressWarnings({"WeakerAccess", "SameParameterValue"})
    public Options setDefaultReportingIntervalMillis(int value) {
        if (maxReportingIntervalMillis != DEFAULT_REPORTING_INTERVAL_MILLIS) {
            return this;
        }
        try {
            return new Options.OptionsBuilder(this).withMaxReportingIntervalMillis(value).build();
        } catch (MalformedURLException e) {
            // not possible given that we are constructing Options from a valid set of Options
            throw new IllegalArgumentException("Unexpected error when building a new set of" +
                    "options from a valid set of existing options. collectorUrl=" +
                    this.collectorUrl);
        }
    }

    /**
     * Provided so implementations of AbstractTracer can turn off resetClient by default.
     * For example, Android tracer may not want resetClient.
     */
    @SuppressWarnings("unused")
    public Options disableResetClient() {
        try {
            return new OptionsBuilder(this).withResetClient(false).build();
        } catch (MalformedURLException e) {
            // not possible given that we are constructing Options from a valid set of Options
            throw new IllegalArgumentException("Unexpected error when building a new set of" +
                "options from a valid set of existing options. collectorUrl=" +
                this.collectorUrl);
        }
    }
}
