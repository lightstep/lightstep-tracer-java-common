package com.lightstep.tracer.shared;

import com.lightstep.tracer.grpc.Auth;
import com.lightstep.tracer.grpc.Command;
import com.lightstep.tracer.grpc.KeyValue;
import com.lightstep.tracer.grpc.ReportRequest;
import com.lightstep.tracer.grpc.ReportResponse;
import com.lightstep.tracer.grpc.Reporter;
import com.lightstep.tracer.grpc.Span;
import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMap;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import static com.lightstep.tracer.shared.AbstractTracer.InternalLogLevel.DEBUG;
import static com.lightstep.tracer.shared.AbstractTracer.InternalLogLevel.ERROR;
import static com.lightstep.tracer.shared.Options.VERBOSITY_DEBUG;
import static com.lightstep.tracer.shared.Options.VERBOSITY_FIRST_ERROR_ONLY;
import static com.lightstep.tracer.shared.Options.VERBOSITY_INFO;

public abstract class AbstractTracer implements Tracer {
    // Maximum interval between reports
    private static final long DEFAULT_CLOCK_STATE_INTERVAL_MILLIS = 500;
    private static final int DEFAULT_CLIENT_RESET_INTERVAL_MILLIS = 5 * 60 * 1000; // 5 min

    private static class ReportResult {
        private final int droppedSpans;
        private final boolean success;

        private ReportResult(int droppedSpans, boolean success) {
            this.droppedSpans = droppedSpans;
            this.success = success;
        }

        public static ReportResult Success() {
            return new ReportResult(0, true);
        }

        public static ReportResult Error(int droppedSpans) {
            return new ReportResult(droppedSpans, false);
        }

        public int getDroppedSpans() {
            return droppedSpans;
        }

        public boolean wasSuccessful() {
            return success;
        }
    }

    @SuppressWarnings("unused")
    protected static final String LIGHTSTEP_TRACER_PLATFORM_KEY = "lightstep.tracer_platform";

    @SuppressWarnings("unused")
    protected static final String LIGHTSTEP_TRACER_PLATFORM_VERSION_KEY = "lightstep.tracer_platform_version";

    @SuppressWarnings("unused")
    protected static final String LIGHTSTEP_TRACER_VERSION_KEY = "lightstep.tracer_version";

    /**
     * For mapping internal logs to Android log levels without importing Android
     * packages.
     */
    protected enum InternalLogLevel {
        DEBUG,
        INFO,
        WARN,
        ERROR
    }

    private final int verbosity;
    private final Auth.Builder auth;
    private final Reporter.Builder reporter;
    private final CollectorClient client;
    private final ClientMetrics clientMetrics;

    /**
     * False, until the first error has been logged, after which it is true, and if verbosity says
     * not to log more than one error, no more errors will be logged.
     */
    private boolean firstErrorLogged = false;

    // Timestamp of the last recorded span. Used to terminate the reporting
    // loop thread if no new data has come in (which is necessary for clean
    // shutdown).
    private final AtomicLong lastNewSpanMillis;
    private ArrayList<Span> spans;
    private final ClockState clockState;

    // Should *NOT* attempt to take a span's lock while holding this lock.
    @SuppressWarnings("WeakerAccess")
    protected final Object mutex = new Object();
    private boolean reportInProgress;

    // This is set to non-null if background reporting is enabled.
    private ReportingLoop reportingLoop;

    private final int maxBufferedSpans;

    // This is set to non-null when a background Thread is actually reporting.
    private Thread reportingThread;

    private boolean isDisabled;

    private boolean resetClient;

    private final ScopeManager scopeManager;

    private final Map<Format<?>, Propagator<?>> propagators;

    public AbstractTracer(Options options) {
        scopeManager = options.scopeManager;
        // Set verbosity first so debug logs from the constructor take effect
        verbosity = options.verbosity;

        // Save the maxBufferedSpans since we need it post-construction, too.
        maxBufferedSpans = options.maxBufferedSpans;

        // TODO sanity check options
        lastNewSpanMillis = new AtomicLong(System.currentTimeMillis());
        spans = new ArrayList<>(maxBufferedSpans);

        // Unfortunately Java7 has no way to generate a timestamp that's both
        // precise (a la System.nanoTime()) and absolute (a la
        // System.currentTimeMillis()). We store an absolute start timestamp but at
        // least get a precise duration at Span.finish() time via
        // startTimestampRelativeNanos (search for it below).
        if (options.useClockCorrection) {
            clockState = new ClockState();
        } else {
            clockState = new ClockState.NoopClockState();
        }

        auth = Auth.newBuilder().setAccessToken(options.accessToken);
        reporter = Reporter.newBuilder().setReporterId(options.getGuid());
        resetClient = options.resetClient;
        clientMetrics = new ClientMetrics();

        // initialize collector client
        boolean validCollectorClient = true;
        client = CollectorClientProvider.provider()
                .forOptions(this, options);
        if (client == null) {
            error("Exception creating client.");
            validCollectorClient = false;
            disable();
        }

        for (Map.Entry<String, Object> entry : options.tags.entrySet()) {
            addTracerTag(entry.getKey(), entry.getValue());
        }

        if (validCollectorClient && !options.disableReportingLoop) {
            reportingLoop = new ReportingLoop(options.maxReportingIntervalMillis);
        }

        propagators = options.propagators;
    }

    /**
     * This call is NOT synchronized
     */
    private void doStopReporting() {
        synchronized (this) {
            // Note: There is no synchronization to prevent multiple
            // reporting loops from running simultaneously.  It's possible
            // for one to start before another one exits, which is safe
            // because flushInternal() is itself synchronized.
            if (reportingThread == null) {
                return;
            }
            reportingThread.interrupt();
            reportingThread = null;
        }
    }

    /**
     * This call is synchronized
     */
    private void maybeStartReporting() {
        if (reportingThread != null) {
            return;
        }
        reportingThread = new Thread(reportingLoop);
        reportingThread.setDaemon(true);
        reportingThread.start();
    }

    /**
     * Runs a relatively frequent loop in a separate thread to check if the
     * library should flush its current buffer or if the loop should stop.
     *
     * In the JRE case, the actual flush will be run in this thread. In the
     * case of Android, this thread will block and wait until the Android
     * AsyncTask finishes.
     */
    private class ReportingLoop implements Runnable {
        // Controls how often the reporting loop itself checks if the status.
        private static final int POLL_INTERVAL_MILLIS = 40;

        private static final int THREAD_TIMEOUT_MILLIS = 2000;

        private Random rng = new Random(System.currentTimeMillis());
        private long reportingIntervalMillis = 0;
        private int consecutiveFailures = 0;

        ReportingLoop(long interval) {
            reportingIntervalMillis = interval;
        }

        @Override
        public void run() {
            debug("Reporting thread started");
            long nextReportMillis = computeNextReportMillis();
            long nextResetMillis = System.currentTimeMillis() + DEFAULT_CLIENT_RESET_INTERVAL_MILLIS;

            // Run until the reporting loop has been explicitly told to stop.
            while (!Thread.interrupted()) {
                // Check if it's time to attempt the next report. At this point, the
                // report may not actually result in network traffic if the there's
                // no new data to report or, for example, the Android device does
                // not have a wireless connection.
                long nowMillis = System.currentTimeMillis();
                if (resetClient && nowMillis >= nextResetMillis) {
                    client.reconnect();
                    nextResetMillis = System.currentTimeMillis() + DEFAULT_CLIENT_RESET_INTERVAL_MILLIS;
                }
                if (spans.size() >= (maxBufferedSpans/2) || nowMillis >= nextReportMillis) {
                    SimpleFuture<Boolean> result = flushInternal(false);
                    boolean reportSucceeded = false;
                    try {
                        reportSucceeded = result.get();
                    } catch (InterruptedException e) {
                        warn("Future timed out");
                        Thread.currentThread().interrupt();
                    }

                    // Check consecutive failures for back off purposes
                    if (!reportSucceeded) {
                        consecutiveFailures++;
                    } else {
                        consecutiveFailures = 0;
                    }
                    nextReportMillis = computeNextReportMillis();
                }

                // If the tracer hasn't received new data in a while, stop the
                // reporting loop. It will be restarted when the next span is finished.
                boolean hasUnreportedSpans = (unreportedSpanCount() > 0);
                long lastSpanAgeMillis = System.currentTimeMillis() - lastNewSpanMillis.get();
                if ((!hasUnreportedSpans || consecutiveFailures >= 2) &&
                        lastSpanAgeMillis > THREAD_TIMEOUT_MILLIS) {
                    doStopReporting();
                } else {
                    try {
                        Thread.sleep(POLL_INTERVAL_MILLIS);
                    } catch (InterruptedException e) {
                        warn("Exception trying to sleep in reporting thread");
                        Thread.currentThread().interrupt();
                    }
                }
            }
            debug("Reporting thread stopped");
        }

        /**
         * Compute the next time, as compared to System.currentTimeMillis(), that
         * a report should be attempted.  Accounts for clock state, error back off,
         * and random jitter.
         */
        long computeNextReportMillis() {
            double base;
            if (!clockState.isReady()) {
                base = (double) DEFAULT_CLOCK_STATE_INTERVAL_MILLIS;
            } else {
                base = (double) reportingIntervalMillis;
            }

            // Exponential back off based on number of consecutive errors, up to 8x the normal
            // interval
            int backOff = 1 + Math.min(7, consecutiveFailures);
            base *= (double) backOff;

            // Add +/- 10% jitter to the regular reporting interval
            final double delta = base * (0.9 + 0.2 * rng.nextDouble());
            final long nextMillis = System.currentTimeMillis() + (long) Math.ceil(delta);
            debug(String.format("Next report: %d (%f) [%d]", nextMillis, delta, clockState.activeSampleCount()));
            return nextMillis;
        }
    }

    /**
     * Disable the tracer, stopping any further reports and turning all
     * subsequent method invocations into no-ops.
     */
    private void disable() {
        info("Disabling client library");
        doStopReporting();

        synchronized (mutex) {
            if (client != null ) {
                client.shutdown();
            }
            isDisabled = true;

            // The code makes various assumptions about this field never being
            // null, so replace it with an empty list rather than nulling it out.
            spans = new ArrayList<>(0);
        }
    }

    @SuppressWarnings("WeakerAccess")
    public boolean isDisabled() {
        synchronized (mutex) {
            return isDisabled;
        }
    }

    @Override
    public ScopeManager scopeManager() {
        return scopeManager;
    }

    @Override
    public io.opentracing.Span activeSpan() {
        Scope scope = scopeManager.active();
        return scope == null ? null : scope.span();
    }

    @Override
    public io.opentracing.Scope activateSpan(io.opentracing.Span span) {
        return scopeManager.activate(span);
    }

    public Tracer.SpanBuilder buildSpan(String operationName) {
        return new com.lightstep.tracer.shared.SpanBuilder(operationName, this);
    }

    public <C> void inject(io.opentracing.SpanContext spanContext, Format<C> format, C carrier) {
        if ( !(spanContext instanceof SpanContext) ) {
            error("Unsupported SpanContext implementation: " + spanContext.getClass());
            return;
        }
        SpanContext lightstepSpanContext = (SpanContext) spanContext;

        if (!propagators.containsKey(format)) {
            info("Unsupported carrier type: " + carrier.getClass());
            return;
        }

        Propagator<C> propagator = (Propagator<C>) propagators.get(format);
        propagator.inject(lightstepSpanContext, carrier);
    }

    public <C> io.opentracing.SpanContext extract(Format<C> format, C carrier) {
        if (!propagators.containsKey(format)) {
            info("Unsupported carrier type: " + carrier.getClass());
            return null;
        }

        Propagator<C> propagator = (Propagator<C>) propagators.get(format);
        return propagator.extract(carrier);
    }

    /**
     * Initiates a flush of data to the collectors. Method does not return until the flush is
     * complete, or has timed out.
     *
     * @param timeoutMillis The amount of time, in milliseconds, to allow for the flush to complete
     * @return True if the flush completed within the time allotted, false otherwise.
     */
    @SuppressWarnings("WeakerAccess")
    public Boolean flush(long timeoutMillis) {
        SimpleFuture<Boolean> flushFuture = flushInternal(true);
        try {
            return flushFuture.getWithTimeout(timeoutMillis);
        } catch (InterruptedException e) {
            return false;
        }
    }

    protected abstract SimpleFuture<Boolean> flushInternal(boolean explicitRequest);

    /**
     * Does the work of a flush by sending spans to the collector.
     *
     * @param explicitRequest if true, the report request was made explicitly rather than implicitly
     *                        (via a reporting loop) and therefore the code should make a 'best
     *                        effort' to truly report (i.e. send even if the clock state is not
     *                        ready).
     * @return true if the report was sent successfully
     */
    @SuppressWarnings("unused")
    protected boolean sendReport(boolean explicitRequest) {
        synchronized (mutex) {
            if (reportInProgress) {
                debug("Report in progress. Skipping.");
                return true;
            }
            if (spans.size() == 0 && clockState.isReady()) {
                debug("Skipping report. No new data.");
                return true;
            }

            // Make sure other threads don't try to start sending a report.
            reportInProgress = true;
        }

        try {
            ReportResult result = sendReportWorker(explicitRequest);
            this.clientMetrics.addSpansDropped(result.getDroppedSpans());
            return result.wasSuccessful();
        } finally {
            synchronized (mutex) {
                reportInProgress = false;
            }
        }
    }

    /**
     * Returns the number of currently unreported (buffered) spans.
     *
     * Note: this method acquires the mutex. In Java synchronized locks are reentrant, but if the
     * lock is already acquired, calling spans.size() directly should suffice.
     */
    private int unreportedSpanCount() {
        synchronized (mutex) {
            return spans.size();
        }
    }

    /**
     * Private worker function for sendReport() to make the locking and guard
     * variable bracketing a little more straightforward.
     *
     * @return the number of dropped spans.
     */
    private ReportResult sendReportWorker(boolean explicitRequest) {
        // Data to be sent.
        ArrayList<Span> spans;

        synchronized (mutex) {
            if (clockState.isReady() || explicitRequest) {
                // Copy the reference to the spans and make a new array for other spans.
                spans = this.spans;
                this.spans = new ArrayList<>(maxBufferedSpans);
                debug(String.format("Sending report, %d spans", spans.size()));
            } else {
                // Otherwise, if the clock state is not ready, we'll send an empty
                // report.
                debug("Sending empty report to prime clock state");
                spans = new ArrayList<>();
            }
        }

        ReportRequest request = ReportRequest.newBuilder()
                .setReporter(reporter)
                .setAuth(auth)
                .addAllSpans(spans)
                .setTimestampOffsetMicros(clockState.offsetMicros())
                .setInternalMetrics(clientMetrics.toInternalMetricsAndReset())
                .build();

        long originMicros = Util.nowMicrosApproximate();
        long originRelativeNanos = System.nanoTime();

        ReportResponse response = null;
        if (client != null) {
            response = client.report(request);
        }

        if (response == null) {
            return ReportResult.Error(spans.size());
        }

        if (!response.getErrorsList().isEmpty()) {
            List<String> errs = response.getErrorsList();
            for (String err : errs) {
                this.error("Collector response contained error: ", err);
            }
            return ReportResult.Error(spans.size());
        }

        if (response.hasReceiveTimestamp() && response.hasTransmitTimestamp()) {
            long deltaMicros = (System.nanoTime() - originRelativeNanos) / 1000;
            long destinationMicros = originMicros + deltaMicros;
            clockState.addSample(
                    originMicros,
                    Util.protoTimeToEpochMicros(response.getReceiveTimestamp()),
                    Util.protoTimeToEpochMicros(response.getTransmitTimestamp()),
                    destinationMicros
            );
        } else {
            warn("Collector response did not include timing info");
        }

        // Check whether or not to disable the tracer
        if (response.getCommandsCount() != 0) {
            for (Command command : response.getCommandsList()) {
                if (command.getDisable()) {
                    disable();
                }
            }
        }

        debug(String.format("Report sent successfully (%d spans)", spans.size()));

        return ReportResult.Success();
    }

    /**
     * Adds a span to the buffer.
     *
     * @param span the span to be added
     */
    void addSpan(Span span) {
        lastNewSpanMillis.set(System.currentTimeMillis());

        synchronized (mutex) {
            if (spans.size() >= maxBufferedSpans) {
                clientMetrics.addSpansDropped(1);
            } else {
                spans.add(span);
            }
            maybeStartReporting();
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected void addTracerTag(String key, Object value) {
        debug("Adding tracer tag: " + key + " => " + value);
        if (value instanceof String) {
            reporter.addTags(KeyValue.newBuilder().setKey(key).setStringValue((String) value));
        } else if (value instanceof Boolean) {
            reporter.addTags(KeyValue.newBuilder().setKey(key).setBoolValue((Boolean) value));
        } else if (value instanceof Number) {
            if (value instanceof Long || value instanceof Integer) {
                reporter.addTags(KeyValue.newBuilder().setKey(key).setIntValue(((Number) value).longValue()));
            } else if (value instanceof Double || value instanceof Float) {
                reporter.addTags(KeyValue.newBuilder().setKey(key).setDoubleValue(((Number) value).doubleValue()));
            } else {
                reporter.addTags(KeyValue.newBuilder().setKey(key).setStringValue(value.toString()));
            }
        } else {
            reporter.addTags(KeyValue.newBuilder().setKey(key).setStringValue(value.toString()));
        }
    }

    /**
     * Internal logging.
     */
    @SuppressWarnings("WeakerAccess")
    protected void debug(String s) {
        debug(s, null);
    }

    /**
     * Internal logging.
     */
    @SuppressWarnings({"WeakerAccess", "SameParameterValue"})
    protected void debug(String msg, Object payload) {
        if (verbosity < VERBOSITY_DEBUG) {
            return;
        }
        printLogToConsole(DEBUG, msg, payload);
    }

    /**
     * Internal logging.
     */
    @SuppressWarnings("WeakerAccess")
    protected void info(String s) {
        info(s, null);
    }

    /**
     * Internal logging.
     */
    @SuppressWarnings({"WeakerAccess", "SameParameterValue"})
    protected void info(String msg, Object payload) {
        if (verbosity < VERBOSITY_INFO) {
            return;
        }
        printLogToConsole(InternalLogLevel.INFO, msg, payload);
    }

    /**
     * Internal logging.
     */
    @SuppressWarnings("WeakerAccess")
    protected void warn(String s) {
        warn(s, null);
    }

    /**
     * Internal warning.
     */
    @SuppressWarnings({"WeakerAccess", "SameParameterValue"})
    protected void warn(String msg, Object payload) {
        if (verbosity < VERBOSITY_INFO) {
            return;
        }
        printLogToConsole(InternalLogLevel.WARN, msg, payload);
    }

    /**
     * Internal logging.
     */
    @SuppressWarnings("WeakerAccess")
    protected void error(String s) {
        error(s, null);
    }

    /**
     * Internal error.
     */
    @SuppressWarnings("WeakerAccess")
    protected void error(String msg, Object payload) {
        if (verbosity < VERBOSITY_FIRST_ERROR_ONLY) {
            return;
        }
        if (verbosity == VERBOSITY_FIRST_ERROR_ONLY && firstErrorLogged) {
            return;
        }
        firstErrorLogged = true;
        printLogToConsole(ERROR, msg, payload);
    }

    protected abstract void printLogToConsole(InternalLogLevel level, String msg, Object payload);


    String generateTraceURL(long spanId) {
        return "https://app.lightstep.com/" + auth.getAccessToken() +
                "/trace?span_guid=" + Long.toHexString(spanId) +
                "&at_micros=" + Util.nowMicrosApproximate();
    }

    /**
     * Internal method used primarily for unit testing and debugging. This is not
     * part of the OpenTracing API and is not a supported API.
     *
     * Copies the internal state/status into an object that's easier to check
     * against in unit tests.
     */
    @SuppressWarnings("unused")
    public Status status() {
        synchronized (mutex) {
            long spansDropped = 0;
            if (client != null) {
                spansDropped = clientMetrics.getSpansDropped();
            }
            return new Status(reporter.getTagsList(), spansDropped);
        }
    }
}
